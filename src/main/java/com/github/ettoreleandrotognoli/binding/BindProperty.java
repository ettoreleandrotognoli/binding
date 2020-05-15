package com.github.ettoreleandrotognoli.binding;


import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindProperty {

    UpdateStrategy updateStrategy() default UpdateStrategy.READ_WRITE;

    String componentProperty() default "text";

    String modelProperty() default "${model}";

}
