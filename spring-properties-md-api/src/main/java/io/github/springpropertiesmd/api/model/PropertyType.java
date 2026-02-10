package io.github.springpropertiesmd.api.model;

import java.util.List;

public sealed interface PropertyType {

    record SimpleType(String fqcn, String displayName) implements PropertyType {
    }

    record CollectionType(String collectionFqcn, PropertyType elementType) implements PropertyType {
    }

    record MapType(PropertyType keyType, PropertyType valueType) implements PropertyType {
    }

    record EnumType(String fqcn, List<String> allowedValues) implements PropertyType {
        public EnumType {
            allowedValues = List.copyOf(allowedValues);
        }
    }

    record NestedType(String fqcn, String groupName) implements PropertyType {
    }
}
