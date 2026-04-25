package io.github.springpropertiesmd.core.check;

public record ConditionCheckConfig(
        boolean failOnUndocumentedLocalConditionProperty,
        boolean warnOnExternalConditionProperty,
        boolean warnOnCollectionConditionProperty,
        boolean warnOnNonDashedConditionName
) {
    public static ConditionCheckConfig defaults() {
        return new ConditionCheckConfig(true, true, true, true);
    }
}
