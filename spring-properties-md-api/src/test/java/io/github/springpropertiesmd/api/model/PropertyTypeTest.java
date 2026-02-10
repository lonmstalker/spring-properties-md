package io.github.springpropertiesmd.api.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTypeTest {

    @Test
    void simpleTypeAccessors() {
        var type = new PropertyType.SimpleType("java.lang.String", "String");
        assertThat(type.fqcn()).isEqualTo("java.lang.String");
        assertThat(type.displayName()).isEqualTo("String");
        assertThat(type).isInstanceOf(PropertyType.class);
    }

    @Test
    void collectionTypeAccessors() {
        var elementType = new PropertyType.SimpleType("java.lang.String", "String");
        var type = new PropertyType.CollectionType("java.util.List", elementType);
        assertThat(type.collectionFqcn()).isEqualTo("java.util.List");
        assertThat(type.elementType()).isEqualTo(elementType);
    }

    @Test
    void mapTypeAccessors() {
        var keyType = new PropertyType.SimpleType("java.lang.String", "String");
        var valueType = new PropertyType.SimpleType("java.lang.Integer", "Integer");
        var type = new PropertyType.MapType(keyType, valueType);
        assertThat(type.keyType()).isEqualTo(keyType);
        assertThat(type.valueType()).isEqualTo(valueType);
    }

    @Test
    void enumTypeAccessors() {
        var type = new PropertyType.EnumType("com.example.Level", List.of("LOW", "MEDIUM", "HIGH"));
        assertThat(type.fqcn()).isEqualTo("com.example.Level");
        assertThat(type.allowedValues()).containsExactly("LOW", "MEDIUM", "HIGH");
    }

    @Test
    void enumTypeDefensiveCopy() {
        var values = new java.util.ArrayList<>(List.of("A", "B"));
        var type = new PropertyType.EnumType("com.example.E", values);
        values.add("C");
        assertThat(type.allowedValues()).containsExactly("A", "B");
    }

    @Test
    void enumTypeAllowedValuesUnmodifiable() {
        var type = new PropertyType.EnumType("com.example.E", List.of("A"));
        assertThatThrownBy(() -> type.allowedValues().add("B"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nestedTypeAccessors() {
        var type = new PropertyType.NestedType("com.example.Inner", "server.ssl");
        assertThat(type.fqcn()).isEqualTo("com.example.Inner");
        assertThat(type.groupName()).isEqualTo("server.ssl");
    }

    @Test
    void patternMatchingOnSealedInterface() {
        PropertyType type = new PropertyType.SimpleType("java.lang.String", "String");

        String result = switch (type) {
            case PropertyType.SimpleType s -> "simple:" + s.displayName();
            case PropertyType.CollectionType c -> "collection:" + c.collectionFqcn();
            case PropertyType.MapType m -> "map";
            case PropertyType.EnumType e -> "enum:" + e.fqcn();
            case PropertyType.NestedType n -> "nested:" + n.groupName();
        };

        assertThat(result).isEqualTo("simple:String");
    }
}
