package io.github.springpropertiesmd.core.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratorConfigTest {

    @Test
    void defaultsHaveExpectedValues() {
        GeneratorConfig config = GeneratorConfig.defaults(Path.of("output.md"));

        assertThat(config.outputFile()).isEqualTo(Path.of("output.md"));
        assertThat(config.title()).isEqualTo("Configuration Properties");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
        assertThat(config.includeTableOfContents()).isTrue();
        assertThat(config.includeDeprecated()).isTrue();
        assertThat(config.includeValidation()).isTrue();
        assertThat(config.includeExamples()).isTrue();
        assertThat(config.includeSensitive()).isTrue();
        assertThat(config.includeCustomMetadata()).isFalse();
    }

    @Test
    void customConfig() {
        GeneratorConfig config = new GeneratorConfig(
                Path.of("custom.md"),
                "My Docs",
                OutputStyle.PER_GROUP,
                false, false, false, false, false, true
        );

        assertThat(config.outputFile()).isEqualTo(Path.of("custom.md"));
        assertThat(config.title()).isEqualTo("My Docs");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.PER_GROUP);
        assertThat(config.includeTableOfContents()).isFalse();
        assertThat(config.includeCustomMetadata()).isTrue();
    }
}
