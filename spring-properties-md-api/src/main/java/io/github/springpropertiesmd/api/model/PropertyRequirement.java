package io.github.springpropertiesmd.api.model;

public record PropertyRequirement(
        String propertyName,
        String havingValue,
        boolean matchIfMissing,
        PropertyConditionMatchMode matchMode,
        boolean local
) {
    public PropertyRequirement {
        matchMode = matchMode != null ? matchMode : PropertyConditionMatchMode.PRESENT_AND_NOT_FALSE;
    }
}
