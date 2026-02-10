package io.github.springpropertiesmd.api.model;

public record DeprecationInfo(
        String reason,
        String replacedBy,
        String since,
        String removalVersion
) {
}
