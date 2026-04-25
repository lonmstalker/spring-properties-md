package io.github.springpropertiesmd.maven;

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
                includeCustomMetadata
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
}
