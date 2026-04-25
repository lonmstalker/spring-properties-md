package io.github.springpropertiesmd.core.config;

import java.nio.file.Path;

public record GeneratorConfig(
        Path outputFile,
        Path outputDirectory,
        String title,
        OutputStyle outputStyle,
        boolean includeTableOfContents,
        boolean includeDeprecated,
        boolean includeValidation,
        boolean includeExamples,
        SensitiveMode sensitiveMode,
        boolean includeCustomMetadata,
        ConditionConfig conditions
) {
    public GeneratorConfig(
            Path outputFile,
            Path outputDirectory,
            String title,
            OutputStyle outputStyle,
            boolean includeTableOfContents,
            boolean includeDeprecated,
            boolean includeValidation,
            boolean includeExamples,
            SensitiveMode sensitiveMode,
            boolean includeCustomMetadata
    ) {
        this(outputFile, outputDirectory, title, outputStyle, includeTableOfContents,
                includeDeprecated, includeValidation, includeExamples, sensitiveMode,
                includeCustomMetadata, null);
    }

    public GeneratorConfig {
        outputDirectory = outputDirectory != null ? outputDirectory : defaultOutputDirectory(outputFile);
        title = title != null ? title : "Configuration Properties";
        outputStyle = outputStyle != null ? outputStyle : OutputStyle.SINGLE_FILE;
        sensitiveMode = sensitiveMode != null ? sensitiveMode : SensitiveMode.REDACT;
        Path defaultExternalConditionsOutputFile = defaultExternalConditionsOutputFile(outputFile);
        conditions = conditions != null
                ? conditions.withDefaults(defaultExternalConditionsOutputFile)
                : ConditionConfig.defaults(defaultExternalConditionsOutputFile);
    }

    public static GeneratorConfig defaults(Path outputFile) {
        return new GeneratorConfig(
                outputFile,
                defaultOutputDirectory(outputFile),
                "Configuration Properties",
                OutputStyle.SINGLE_FILE,
                true,
                true,
                true,
                true,
                SensitiveMode.REDACT,
                false,
                null
        );
    }

    public static Path defaultOutputDirectory(Path outputFile) {
        if (outputFile == null) {
            return Path.of("configuration-properties");
        }
        Path fileName = outputFile.getFileName();
        String name = fileName != null ? fileName.toString() : "configuration-properties.md";
        int dot = name.lastIndexOf('.');
        String baseName = dot > 0 ? name.substring(0, dot) : name;
        Path parent = outputFile.getParent();
        return parent == null ? Path.of(baseName) : parent.resolve(baseName);
    }

    public static Path defaultExternalConditionsOutputFile(Path outputFile) {
        Path fileName = Path.of("external-property-conditions.md");
        if (outputFile == null || outputFile.getParent() == null) {
            return fileName;
        }
        return outputFile.getParent().resolve(fileName);
    }
}
