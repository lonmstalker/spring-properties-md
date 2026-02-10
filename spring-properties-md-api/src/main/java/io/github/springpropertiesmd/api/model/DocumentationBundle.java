package io.github.springpropertiesmd.api.model;

import java.util.List;

public record DocumentationBundle(
        List<GroupMetadata> groups,
        List<PropertyMetadata> properties
) {
    public DocumentationBundle {
        groups = groups == null ? List.of() : List.copyOf(groups);
        properties = properties == null ? List.of() : List.copyOf(properties);
    }

    public static DocumentationBundle empty() {
        return new DocumentationBundle(List.of(), List.of());
    }
}
