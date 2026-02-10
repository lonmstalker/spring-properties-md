package io.github.springpropertiesmd.api.model;

import java.util.List;
import java.util.Map;

public record PropertyMetadata(
        String name,
        String type,
        String typeDisplay,
        String description,
        String defaultValue,
        boolean required,
        boolean sensitive,
        List<String> profiles,
        DeprecationInfo deprecation,
        List<ExampleValue> examples,
        List<ValidationConstraint> constraints,
        String category,
        String subcategory,
        String since,
        List<String> seeAlso,
        Map<String, String> customMetadata,
        String sourceType,
        String groupName
) {
    public PropertyMetadata {
        profiles = profiles == null ? List.of() : List.copyOf(profiles);
        examples = examples == null ? List.of() : List.copyOf(examples);
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        seeAlso = seeAlso == null ? List.of() : List.copyOf(seeAlso);
        customMetadata = customMetadata == null ? Map.of() : Map.copyOf(customMetadata);
    }
}
