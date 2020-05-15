package com.github.ettoreleandrotognoli.binding;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.*;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModelSupport<E extends Model> implements ModelHolder<E> {

    transient private BindingGroup bindingGroup = null;
    transient private List<PropertyChangeListener> propertyLinks = null;

    protected Object view;
    protected E model;

    public ModelSupport(Object view) {
        this.view = view;
    }

    @Override
    public E getModel() {
        return model;
    }

    @Override
    public void setModel(E model) {
        if (this.bindingGroup != null) {
            this.destroyBind();
            this.unlinkProperties();
        }
        this.model = model;
        this.createBind();
        this.linkProperties();
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
            ELProperty<Object, Object> elProperty = ELProperty.create("${model." + bindProperty.modelProperty() + "}");
            BeanProperty<Object, Object> beanProperty = BeanProperty.create(bindProperty.componentProperty());
            UpdateStrategy updateStrategy = bindProperty.updateStrategy();
            Object component;
            try {
                field.setAccessible(true);
                component = field.get(this.view);
                Binding<Object, Object, Object, Object> bind = Bindings.createAutoBinding(updateStrategy, this.view, elProperty, component, beanProperty);
                this.bindingGroup.addBinding(bind);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
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
            if (linkProperty.value() == null || linkProperty.value().equals("")) {
                this.model.addPropertyChangeListener(listener);
            } else {
                this.model.addPropertyChangeListener(linkProperty.value(), listener);
            }
            links.add(listener);
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
