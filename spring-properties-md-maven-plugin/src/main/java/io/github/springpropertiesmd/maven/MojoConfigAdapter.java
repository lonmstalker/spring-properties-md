package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.core.check.ConditionCheckConfig;
import io.github.springpropertiesmd.core.config.ConditionConfig;
import io.github.springpropertiesmd.core.config.ExternalConditionMode;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;

import java.nio.file.Path;

public class MojoConfigAdapter {

    public GeneratorConfig adapt(
            Path outputFile,
            Path outputDirectory,
            String title,
            String outputStyle,
            String sensitiveMode,
            boolean includeTableOfContents,
            boolean includeDeprecated,
            boolean includeValidation,
            boolean includeExamples,
            boolean includeCustomMetadata
    ) {
        return new GeneratorConfig(
                outputFile,
                outputDirectory,
                title != null ? title : "Configuration Properties",
                outputStyleOf(outputStyle),
                includeTableOfContents,
                includeDeprecated,
                includeValidation,
                includeExamples,
                sensitiveModeOf(sensitiveMode),
                includeCustomMetadata,
                null
        );
    }

    public GeneratorConfig adapt(
            Path outputFile,
            Path outputDirectory,
            String title,
            String outputStyle,
            String sensitiveMode,
            boolean includeTableOfContents,
            boolean includeDeprecated,
            boolean includeValidation,
            boolean includeExamples,
            boolean includeCustomMetadata,
            ConditionsMojoConfig conditions
    ) {
        return new GeneratorConfig(
                outputFile,
                outputDirectory,
                title != null ? title : "Configuration Properties",
                outputStyleOf(outputStyle),
                includeTableOfContents,
                includeDeprecated,
                includeValidation,
                includeExamples,
                sensitiveModeOf(sensitiveMode),
                includeCustomMetadata,
                conditionConfig(conditions)
        );
    }

    public ConditionCheckConfig conditionCheckConfig(ConditionsMojoConfig conditions) {
        ConditionsMojoConfig.Checks checks = conditions != null ? conditions.checks() : new ConditionsMojoConfig.Checks();
        return new ConditionCheckConfig(
                checks.failOnUndocumentedLocalConditionProperty(),
                checks.warnOnExternalConditionProperty(),
                checks.warnOnCollectionConditionProperty(),
                checks.warnOnNonDashedConditionName()
        );
    }

    private OutputStyle outputStyleOf(String outputStyle) {
        try {
            return OutputStyle.valueOf(outputStyle);
        } catch (IllegalArgumentException | NullPointerException e) {
            return OutputStyle.SINGLE_FILE;
        }
    }

    private SensitiveMode sensitiveModeOf(String sensitiveMode) {
        try {
            return SensitiveMode.valueOf(sensitiveMode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return SensitiveMode.REDACT;
        }
    }

    private ConditionConfig conditionConfig(ConditionsMojoConfig conditions) {
        ConditionsMojoConfig config = conditions != null ? conditions : new ConditionsMojoConfig();
        return new ConditionConfig(
                config.enabled(),
                config.springConditionalOnProperty(),
                externalConditionModeOf(config.externalConditionMode()),
                config.externalConditionsOutputFile() != null ? Path.of(config.externalConditionsOutputFile()) : null
        );
    }

    private ExternalConditionMode externalConditionModeOf(String externalConditionMode) {
        try {
            return ExternalConditionMode.valueOf(externalConditionMode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ExternalConditionMode.WARN;
        }
    }
}
