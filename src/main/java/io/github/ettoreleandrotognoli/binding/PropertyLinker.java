package io.github.ettoreleandrotognoli.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyLinker implements PropertyChangeListener {

    private Object actor;
    private Method action;

    public PropertyLinker(Object actor, Method action) {
        this.actor = actor;
        this.action = action;
        this.action.setAccessible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            this.executeAction(evt);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void executeAction(PropertyChangeEvent evt) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Class<?>[] parameterTypes = action.getParameterTypes();

        if (parameterTypes.length == 1
                && PropertyChangeEvent.class.isAssignableFrom(parameterTypes[0])) {
            action.invoke(actor, evt);

        } else if (parameterTypes.length == 0) {
            action.invoke(actor);
        } else {
            Object[] args = new Object[parameterTypes.length];
            action.invoke(actor, args);
        }
    }

}
