package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MojoConfigAdapterTest {

    private final MojoConfigAdapter adapter = new MojoConfigAdapter();

    @Test
    void adaptWithAllDefaults() {
        GeneratorConfig config = adapter.adapt(
                Path.of("output.md"), "Test Title", "SINGLE_FILE",
                true, true, true, true, true, false
        );

        assertThat(config.outputFile()).isEqualTo(Path.of("output.md"));
        assertThat(config.title()).isEqualTo("Test Title");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
        assertThat(config.includeTableOfContents()).isTrue();
    }

    @Test
    void adaptWithPerGroupStyle() {
        GeneratorConfig config = adapter.adapt(
                Path.of("out.md"), "Title", "PER_GROUP",
                false, false, false, false, false, true
        );

        assertThat(config.outputStyle()).isEqualTo(OutputStyle.PER_GROUP);
        assertThat(config.includeTableOfContents()).isFalse();
        assertThat(config.includeCustomMetadata()).isTrue();
    }

    @Test
    void adaptWithInvalidStyleFallsBackToDefault() {
        GeneratorConfig config = adapter.adapt(
                Path.of("out.md"), "Title", "INVALID",
                true, true, true, true, true, false
        );

        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
    }

    @Test
    void adaptWithNullTitleUsesDefault() {
        GeneratorConfig config = adapter.adapt(
                Path.of("out.md"), null, "SINGLE_FILE",
                true, true, true, true, true, false
        );

        assertThat(config.title()).isEqualTo("Configuration Properties");
    }
}
