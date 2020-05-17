package com.github.ettoreleandrotognoli.binding;

@FunctionalInterface
public interface PropertyChangeTrigger {
    void firePropertyChange(String propertyName, Object oldValue, Object newValue);
}
