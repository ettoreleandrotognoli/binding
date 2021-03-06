package com.github.ettoreleandrotognoli.binding;

import org.jdesktop.beansbinding.AutoBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindProperty {
    AutoBinding.UpdateStrategy updateStrategy() default AutoBinding.UpdateStrategy.READ_WRITE;

    String componentProperty() default "value";

    String modelProperty() default "${model}";

}
