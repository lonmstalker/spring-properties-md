package io.github.springpropertiesmd.core.check;

public record CheckConfig(
        boolean failOnMissingDescription,
        boolean failOnSensitiveDefault,
        boolean failOnDeprecatedWithoutReplacement,
        boolean failOnRequiredWithoutExample,
        boolean failOnDuplicatePropertyNames,
        boolean failIfGeneratedDocsChanged,
        ConditionCheckConfig conditions
) {
    public CheckConfig(
            boolean failOnMissingDescription,
            boolean failOnSensitiveDefault,
            boolean failOnDeprecatedWithoutReplacement,
            boolean failOnRequiredWithoutExample,
            boolean failOnDuplicatePropertyNames,
            boolean failIfGeneratedDocsChanged
    ) {
        this(failOnMissingDescription, failOnSensitiveDefault, failOnDeprecatedWithoutReplacement,
                failOnRequiredWithoutExample, failOnDuplicatePropertyNames, failIfGeneratedDocsChanged, null);
    }

    public CheckConfig {
        conditions = conditions != null ? conditions : ConditionCheckConfig.defaults();
    }

    public static CheckConfig defaults() {
        return new CheckConfig(true, true, true, true, true, false, null);
    }

    public CheckConfig withFailIfGeneratedDocsChanged(boolean value) {
        return new CheckConfig(
                failOnMissingDescription,
                failOnSensitiveDefault,
                failOnDeprecatedWithoutReplacement,
                failOnRequiredWithoutExample,
                failOnDuplicatePropertyNames,
                value,
                conditions
        );
    }
}
