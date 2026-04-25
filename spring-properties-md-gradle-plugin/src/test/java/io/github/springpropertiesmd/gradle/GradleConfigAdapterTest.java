package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.ExternalConditionMode;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;
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
        Path defaultOutputDirectory = Path.of("build/docs");

        GeneratorConfig config = adapter.adapt(extension, defaultOutput, defaultOutputDirectory);

        assertThat(config.outputFile()).isEqualTo(defaultOutput);
        assertThat(config.outputDirectory()).isEqualTo(defaultOutputDirectory);
        assertThat(config.title()).isEqualTo("Configuration Properties");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.SINGLE_FILE);
        assertThat(config.sensitiveMode()).isEqualTo(SensitiveMode.REDACT);
        assertThat(config.includeTableOfContents()).isTrue();
        assertThat(config.conditions().enabled()).isTrue();
        assertThat(config.conditions().springConditionalOnProperty()).isTrue();
        assertThat(config.conditions().externalConditionMode()).isEqualTo(ExternalConditionMode.WARN);
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
        extension.getOutputDirectory().set("/custom/path");
        extension.getSensitiveMode().set("OMIT");
        extension.getIncludeTableOfContents().set(false);
        extension.conditions(conditions -> {
            conditions.getExternalConditionMode().set("SEPARATE_FILE");
            conditions.getExternalConditionsOutputFile().set("/custom/external.md");
        });

        GeneratorConfig config = adapter.adapt(extension, Path.of("default.md"), Path.of("default"));

        assertThat(config.outputFile()).isEqualTo(Path.of("/custom/path.md"));
        assertThat(config.outputDirectory()).isEqualTo(Path.of("/custom/path"));
        assertThat(config.title()).isEqualTo("My Docs");
        assertThat(config.outputStyle()).isEqualTo(OutputStyle.PER_GROUP);
        assertThat(config.sensitiveMode()).isEqualTo(SensitiveMode.OMIT);
        assertThat(config.includeTableOfContents()).isFalse();
        assertThat(config.conditions().externalConditionMode()).isEqualTo(ExternalConditionMode.SEPARATE_FILE);
        assertThat(config.conditions().externalConditionsOutputFile()).isEqualTo(Path.of("/custom/external.md"));
    }
}
