package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GradleConfigAdapterTest {

    private final GradleConfigAdapter adapter = new GradleConfigAdapter();

    @Test
    void adaptWithDefaults() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(SpringPropertiesMdPlugin.class);

        var extension = project.getExtensions().getByType(SpringPropertiesMdExtension.class);
        Path defaultOutput = Path.of("build/docs.md");

        GeneratorConfig config = adapter.adapt(extension, defaultOutput);

        assertThat(config.outputFile()).isEqualTo(defaultOutput);
        assertThat(config.title()).isEqualTo("Configuration Properties");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
        assertThat(config.includeTableOfContents()).isTrue();
    }

    @Test
    void adaptWithCustomValues() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(SpringPropertiesMdPlugin.class);

        var extension = project.getExtensions().getByType(SpringPropertiesMdExtension.class);
        extension.getTitle().set("My Docs");
        extension.getOutputStyle().set("PER_GROUP");
        extension.getOutputFile().set("/custom/path.md");
        extension.getIncludeTableOfContents().set(false);

        GeneratorConfig config = adapter.adapt(extension, Path.of("default.md"));

        assertThat(config.outputFile()).isEqualTo(Path.of("/custom/path.md"));
        assertThat(config.title()).isEqualTo("My Docs");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.PER_GROUP);
        assertThat(config.includeTableOfContents()).isFalse();
    }
}
