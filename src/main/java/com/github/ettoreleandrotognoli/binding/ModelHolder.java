package com.github.ettoreleandrotognoli.binding;


public interface ModelHolder<E> {

    void setModel(E model);

    E getModel();

}
