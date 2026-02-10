package io.github.springpropertiesmd.api.annotation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationRetentionTest {

    @ParameterizedTest
    @ValueSource(classes = {
            PropertyDoc.class,
            PropertyGroupDoc.class,
            PropertyExample.class,
            PropertyDeprecation.class,
            PropertyCategory.class,
            PropertySince.class,
            PropertySee.class,
            PropertyCustomMetadata.class
    })
    void allAnnotationsAreRuntimeRetained(Class<?> annotationClass) {
        var retention = annotationClass.getAnnotation(Retention.class);
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            PropertyDoc.class,
            PropertyGroupDoc.class,
            PropertyExample.class,
            PropertyDeprecation.class,
            PropertyCategory.class,
            PropertySince.class,
            PropertySee.class,
            PropertyCustomMetadata.class
    })
    void allAnnotationsAreDocumented(Class<?> annotationClass) {
        assertThat(annotationClass.isAnnotationPresent(Documented.class)).isTrue();
    }
}
