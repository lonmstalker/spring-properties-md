package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;

import java.nio.file.Path;

public class MojoConfigAdapter {

    public GeneratorConfig adapt(
            Path outputFile,
            String title,
            String outputStyle,
            boolean includeTableOfContents,
            boolean includeDeprecated,
            boolean includeValidation,
            boolean includeExamples,
            boolean includeSensitive,
            boolean includeCustomMetadata
    ) {
        OutputStyle style;
        try {
            style = OutputStyle.valueOf(outputStyle);
        } catch (IllegalArgumentException | NullPointerException e) {
            style = OutputStyle.SINGLE_FILE;
        }

        return new GeneratorConfig(
                outputFile,
                title != null ? title : "Configuration Properties",
                style,
                includeTableOfContents,
                includeDeprecated,
                includeValidation,
                includeExamples,
                includeSensitive,
                includeCustomMetadata
        );
    }
}
