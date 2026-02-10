package io.github.springpropertiesmd.api.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationTargetTest {

    @Test
    void propertyDocTargetsFieldMethodParameter() {
        var targets = PropertyDoc.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
    }

    @Test
    void propertyGroupDocTargetsType() {
        var targets = PropertyGroupDoc.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactly(ElementType.TYPE);
    }

    @Test
    void propertyExampleTargetsFieldMethodParameter() {
        var targets = PropertyExample.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
    }

    @Test
    void propertyDeprecationTargetsFieldMethodType() {
        var targets = PropertyDeprecation.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void propertyCategoryTargetsFieldMethodType() {
        var targets = PropertyCategory.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void propertySinceTargetsFieldMethodType() {
        var targets = PropertySince.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void propertySeeTargetsFieldMethodType() {
        var targets = PropertySee.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void propertyCustomMetadataTargetsFieldMethodType() {
        var targets = PropertyCustomMetadata.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE);
    }
}
