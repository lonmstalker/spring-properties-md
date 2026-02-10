package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;

import java.nio.file.Path;

public class GradleConfigAdapter {

    public GeneratorConfig adapt(SpringPropertiesMdExtension extension, Path defaultOutputFile) {
        Path outputFile = extension.getOutputFile().isPresent()
                ? Path.of(extension.getOutputFile().get())
                : defaultOutputFile;

        OutputStyle style;
        try {
            style = OutputStyle.valueOf(extension.getOutputStyle().get());
        } catch (IllegalArgumentException e) {
            style = OutputStyle.SINGLE_FILE;
        }

        return new GeneratorConfig(
                outputFile,
                extension.getTitle().get(),
                style,
                extension.getIncludeTableOfContents().get(),
                extension.getIncludeDeprecated().get(),
                extension.getIncludeValidation().get(),
                extension.getIncludeExamples().get(),
                extension.getIncludeSensitive().get(),
                extension.getIncludeCustomMetadata().get()
        );
    }
}
