package io.github.springpropertiesmd.api.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationTargetTest {

    @Test
    void propertyDocTargetsFieldMethodParameterRecordComponent() {
        var targets = PropertyDoc.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT);
    }

    @Test
    void propertyGroupDocTargetsType() {
        var targets = PropertyGroupDoc.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactly(ElementType.TYPE);
    }

    @Test
    void propertyExampleTargetsFieldMethodParameterRecordComponent() {
        var targets = PropertyExample.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT);
    }

    @Test
    void propertyDeprecationTargetsFieldMethodTypeRecordComponent() {
        var targets = PropertyDeprecation.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.RECORD_COMPONENT);
    }

    @Test
    void propertyCategoryTargetsFieldMethodTypeRecordComponent() {
        var targets = PropertyCategory.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.RECORD_COMPONENT);
    }

    @Test
    void propertySinceTargetsFieldMethodTypeRecordComponent() {
        var targets = PropertySince.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.RECORD_COMPONENT);
    }

    @Test
    void propertySeeTargetsFieldMethodTypeRecordComponent() {
        var targets = PropertySee.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.RECORD_COMPONENT);
    }

    @Test
    void propertyCustomMetadataTargetsFieldMethodTypeRecordComponent() {
        var targets = PropertyCustomMetadata.class.getAnnotation(Target.class).value();
        assertThat(targets).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.RECORD_COMPONENT);
    }
}
