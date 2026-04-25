package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.core.check.ConditionCheckConfig;
import io.github.springpropertiesmd.core.config.ConditionConfig;
import io.github.springpropertiesmd.core.config.ExternalConditionMode;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;

import java.nio.file.Path;

public class GradleConfigAdapter {

    public GeneratorConfig adapt(SpringPropertiesMdExtension extension, Path defaultOutputFile,
                                 Path defaultOutputDirectory) {
        Path outputFile = extension.getOutputFile().isPresent()
                ? Path.of(extension.getOutputFile().get())
                : defaultOutputFile;
        Path outputDirectory = extension.getOutputDirectory().isPresent()
                ? Path.of(extension.getOutputDirectory().get())
                : defaultOutputDirectory;

        return new GeneratorConfig(
                outputFile,
                outputDirectory,
                extension.getTitle().get(),
                outputStyleOf(extension.getOutputStyle().get()),
                extension.getIncludeTableOfContents().get(),
                extension.getIncludeDeprecated().get(),
                extension.getIncludeValidation().get(),
                extension.getIncludeExamples().get(),
                sensitiveModeOf(extension.getSensitiveMode().get()),
                extension.getIncludeCustomMetadata().get(),
                conditionConfig(extension.getConditions())
        );
    }

    static ConditionConfig conditionConfig(SpringPropertiesMdExtension.ConditionsExtension conditions) {
        return new ConditionConfig(
                conditions.getEnabled().get(),
                conditions.getSpringConditionalOnProperty().get(),
                externalConditionModeOf(conditions.getExternalConditionMode().get()),
                conditions.getExternalConditionsOutputFile().isPresent()
                        ? Path.of(conditions.getExternalConditionsOutputFile().get())
                        : null
        );
    }

    static ConditionCheckConfig conditionCheckConfig(SpringPropertiesMdExtension.ConditionsExtension conditions) {
        SpringPropertiesMdExtension.ConditionChecksExtension checks = conditions.getChecks();
        return new ConditionCheckConfig(
                checks.getFailOnUndocumentedLocalConditionProperty().get(),
                checks.getWarnOnExternalConditionProperty().get(),
                checks.getWarnOnCollectionConditionProperty().get(),
                checks.getWarnOnNonDashedConditionName().get()
        );
    }

    static OutputStyle outputStyleOf(String outputStyle) {
        try {
            return OutputStyle.valueOf(outputStyle);
        } catch (IllegalArgumentException | NullPointerException e) {
            return OutputStyle.SINGLE_FILE;
        }
    }

    static SensitiveMode sensitiveModeOf(String sensitiveMode) {
        try {
            return SensitiveMode.valueOf(sensitiveMode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return SensitiveMode.REDACT;
        }
    }

    static ExternalConditionMode externalConditionModeOf(String externalConditionMode) {
        try {
            return ExternalConditionMode.valueOf(externalConditionMode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ExternalConditionMode.WARN;
        }
    }
}
