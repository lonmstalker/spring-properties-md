package io.github.springpropertiesmd.api.model;

import java.util.List;

public record PropertyConditionMetadata(
        String sourceElement,
        String ownerId,
        ConditionOwnerType ownerType,
        List<PropertyRequirement> requirements
) {
    public PropertyConditionMetadata {
        ownerType = ownerType != null ? ownerType : ConditionOwnerType.PROPERTY_GROUP;
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
    }
}
