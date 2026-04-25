package io.github.springpropertiesmd.api.model;

import java.util.List;

public record DocumentationBundle(
        List<GroupMetadata> groups,
        List<PropertyMetadata> properties,
        List<PropertyConditionMetadata> conditions
) {
    public DocumentationBundle(List<GroupMetadata> groups, List<PropertyMetadata> properties) {
        this(groups, properties, List.of());
    }

    public DocumentationBundle {
        groups = groups == null ? List.of() : List.copyOf(groups);
        properties = properties == null ? List.of() : List.copyOf(properties);
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
    }

    public static DocumentationBundle empty() {
        return new DocumentationBundle(List.of(), List.of(), List.of());
    }
}
