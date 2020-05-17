package com.github.ettoreleandrotognoli.binding;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.*;

import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ModelSupport<E extends Model> implements ModelHolder<E> {

    class Listener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            trigger.firePropertyChange(String.format("model.%s", evt.getPropertyName()), evt.getOldValue(), evt.getNewValue());
        }
    }

    transient private final EventListenerList listenerList = new EventListenerList();
    transient private final Listener listener = new Listener();
    transient private BindingGroup bindingGroup = null;
    transient private List<PropertyChangeListener> propertyLinks = null;

    protected final Object view;
    protected final PropertyChangeTrigger trigger;
    protected E model;


    public ModelSupport(Object view, PropertyChangeTrigger trigger) {
        this.view = view;
        this.trigger = trigger;
        this.listenerList.add(PropertyChangeListener.class, listener);
    }

    public ModelSupport(PropertyChangeTrigger viewAndTrigger) {
        this.view = viewAndTrigger;
        this.trigger = viewAndTrigger;
        this.listenerList.add(PropertyChangeListener.class, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.listenerList.add(PropertyChangeListener.class, listener);
        if (this.model != null) {
            this.model.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.listenerList.remove(PropertyChangeListener.class, listener);
        if (this.model != null) {
            this.model.removePropertyChangeListener(listener);
        }
    }


    @Override
    public E getModel() {
        return model;
    }

    @Override
    public void setModel(E model) {
        E oldModel = this.model;
        if (this.bindingGroup != null) {
            this.destroyBind();
            this.unlinkProperties();
            for (PropertyChangeListener listener : listenerList.getListeners(PropertyChangeListener.class)) {
                this.model.removePropertyChangeListener(listener);
            }
        }
        this.model = model;
        for (PropertyChangeListener listener : listenerList.getListeners(PropertyChangeListener.class)) {
            this.model.addPropertyChangeListener(listener);
        }
        this.createBind();
        this.linkProperties();
        if (!Objects.equals(oldModel, model)) {
            trigger.firePropertyChange("model", oldModel, model);
        }
    }

    private void createBind() {
        this.bindingGroup = new BindingGroup();
        this.bindProperties();
        this.bindingGroup.bind();
    }

    private void destroyBind() {
        this.bindingGroup.unbind();
        this.bindingGroup = null;
    }

    private void bindProperties() {
        Field fields[] = this.view.getClass().getDeclaredFields();

        for (Field field : fields) {
            BindProperty bindProperty = field.getAnnotation(BindProperty.class);
            if (bindProperty == null) {
                continue;
            }
            ELProperty<Object, Object> viewProperty = ELProperty.create("${model." + bindProperty.modelProperty() + "}");
            BeanProperty<Object, Object> componentProperty = BeanProperty.create(bindProperty.componentProperty());
            UpdateStrategy updateStrategy = bindProperty.updateStrategy();
            Object component;
            try {
                field.setAccessible(true);
                component = field.get(this.view);
                Binding<Object, Object, Object, Object> bind = Bindings.createAutoBinding(updateStrategy, this.view, viewProperty, component, componentProperty);
                this.bindingGroup.addBinding(bind);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void linkProperties() {
        List<PropertyChangeListener> links = new LinkedList<>();
        Method methods[] = this.view.getClass().getDeclaredMethods();

        for (Method method : methods) {
            LinkProperty linkProperty = method.getAnnotation(LinkProperty.class);
            if (linkProperty == null) {
                continue;
            }
            PropertyChangeListener listener = new PropertyLinker(this.view, method);
            for (String value : linkProperty.value()) {
                if (value == null || value.equals("")) {
                    this.model.addPropertyChangeListener(listener);
                } else {
                    this.model.addPropertyChangeListener(value, listener);
                }
                links.add(listener);
            }

        }
        this.propertyLinks = new ArrayList<>(links);
        links.clear();
    }

    private void unlinkProperties() {
        Model model = this.model;
        for (PropertyChangeListener listener : this.propertyLinks) {
            model.removePropertyChangeListener(listener);
        }
        this.propertyLinks.clear();
        this.propertyLinks = null;
    }

}
