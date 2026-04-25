package io.github.springpropertiesmd.core.check;

public record CheckConfig(
        boolean failOnMissingDescription,
        boolean failOnSensitiveDefault,
        boolean failOnDeprecatedWithoutReplacement,
        boolean failOnRequiredWithoutExample,
        boolean failOnDuplicatePropertyNames,
        boolean failIfGeneratedDocsChanged
) {
    public static CheckConfig defaults() {
        return new CheckConfig(true, true, true, true, true, false);
    }

    public CheckConfig withFailIfGeneratedDocsChanged(boolean value) {
        return new CheckConfig(
                failOnMissingDescription,
                failOnSensitiveDefault,
                failOnDeprecatedWithoutReplacement,
                failOnRequiredWithoutExample,
                failOnDuplicatePropertyNames,
                value
        );
    }
}
