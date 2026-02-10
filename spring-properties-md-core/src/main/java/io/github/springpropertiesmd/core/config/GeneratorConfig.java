package io.github.springpropertiesmd.core.config;

import java.nio.file.Path;

public record GeneratorConfig(
        Path outputFile,
        String title,
        OutputStyle outputStyle,
        boolean includeTableOfContents,
        boolean includeDeprecated,
        boolean includeValidation,
        boolean includeExamples,
        boolean includeSensitive,
        boolean includeCustomMetadata
) {
    public static GeneratorConfig defaults(Path outputFile) {
        return new GeneratorConfig(
                outputFile,
                "Configuration Properties",
                OutputStyle.SINGLE_FILE,
                true,
                true,
                true,
                true,
                true,
                false
        );
    }
}
