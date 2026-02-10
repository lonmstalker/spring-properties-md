package io.github.springpropertiesmd.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyDoc {

    String description() default "";

    Requirement required() default Requirement.AUTO;

    String[] profiles() default {};

    boolean sensitive() default false;

    String typeDisplay() default "";
}
