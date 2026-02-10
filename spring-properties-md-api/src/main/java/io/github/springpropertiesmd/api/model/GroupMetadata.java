package io.github.springpropertiesmd.api.model;

public record GroupMetadata(
        String name,
        String displayName,
        String description,
        String sourceType,
        String category,
        int order
) {
}
