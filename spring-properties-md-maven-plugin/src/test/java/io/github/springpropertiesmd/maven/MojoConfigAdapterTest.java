package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MojoConfigAdapterTest {

    private final MojoConfigAdapter adapter = new MojoConfigAdapter();

    @Test
    void adaptWithAllDefaults() {
        GeneratorConfig config = adapter.adapt(
                Path.of("output.md"), Path.of("output"), "Test Title", "SINGLE_FILE", "REDACT",
                true, true, true, true, false
        );

        assertThat(config.outputFile()).isEqualTo(Path.of("output.md"));
        assertThat(config.outputDirectory()).isEqualTo(Path.of("output"));
        assertThat(config.title()).isEqualTo("Test Title");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
        assertThat(config.sensitiveMode()).isEqualTo(SensitiveMode.REDACT);
        assertThat(config.includeTableOfContents()).isTrue();
    }

    @Test
    void adaptWithPerGroupStyle() {
        GeneratorConfig config = adapter.adapt(
                Path.of("out.md"), Path.of("out"), "Title", "PER_GROUP", "OMIT",
                false, false, false, false, true
        );

        assertThat(config.outputStyle()).isEqualTo(OutputStyle.PER_GROUP);
        assertThat(config.sensitiveMode()).isEqualTo(SensitiveMode.OMIT);
        assertThat(config.includeTableOfContents()).isFalse();
        assertThat(config.includeCustomMetadata()).isTrue();
    }

    @Test
    void adaptWithInvalidStyleFallsBackToDefault() {
        GeneratorConfig config = adapter.adapt(
                Path.of("out.md"), Path.of("out"), "Title", "INVALID", "INVALID",
                true, true, true, true, false
        );

        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
        assertThat(config.sensitiveMode()).isEqualTo(SensitiveMode.REDACT);
    }

    @Test
    void adaptWithNullTitleUsesDefault() {
        GeneratorConfig config = adapter.adapt(
                Path.of("out.md"), Path.of("out"), null, "SINGLE_FILE", "REDACT",
                true, true, true, true, false
        );

        assertThat(config.title()).isEqualTo("Configuration Properties");
    }
}
