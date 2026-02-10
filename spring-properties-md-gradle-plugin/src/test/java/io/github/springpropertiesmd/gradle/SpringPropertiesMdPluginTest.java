package io.github.springpropertiesmd.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringPropertiesMdPluginTest {

    @Test
    void pluginRegistersTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(SpringPropertiesMdPlugin.class);

        assertThat(project.getTasks().findByName("generatePropertyDocs")).isNotNull();
    }

    @Test
    void pluginCreatesExtension() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(SpringPropertiesMdPlugin.class);

        assertThat(project.getExtensions().findByType(SpringPropertiesMdExtension.class)).isNotNull();
    }

    @Test
    void taskDependsOnCompileJava() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(SpringPropertiesMdPlugin.class);

        var task = project.getTasks().findByName("generatePropertyDocs");
        assertThat(task).isNotNull();
        assertThat(task.getDependsOn()).isNotEmpty();
    }

    @Test
    void extensionHasDefaultValues() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(SpringPropertiesMdPlugin.class);

        var extension = project.getExtensions().getByType(SpringPropertiesMdExtension.class);

        assertThat(extension.getTitle().get()).isEqualTo("Configuration Properties");
        assertThat(extension.getOutputStyle().get()).isEqualTo("SINGLE_FILE");
        assertThat(extension.getIncludeTableOfContents().get()).isTrue();
        assertThat(extension.getIncludeDeprecated().get()).isTrue();
    }
}
