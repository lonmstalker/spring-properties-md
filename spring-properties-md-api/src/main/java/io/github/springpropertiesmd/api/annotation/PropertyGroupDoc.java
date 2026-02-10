package io.github.springpropertiesmd.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyGroupDoc {

    String displayName() default "";

    String description() default "";

    String category() default "";

    int order() default Integer.MAX_VALUE;
}
