package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.DocumentationFileWriter;
import io.github.springpropertiesmd.core.generator.RenderedDocumentation;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;

@CacheableTask
public abstract class GenerateDocsTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getClassesDir();

    @Internal
    public abstract Property<java.io.File> getOutputFile();

    @Internal
    public abstract DirectoryProperty getOutputDirectory();

    @Optional
    @OutputFile
    public java.io.File getSingleOutputFile() {
        if (GradleConfigAdapter.outputStyleOf(getOutputStyle().get()) != io.github.springpropertiesmd.core.config.OutputStyle.SINGLE_FILE) {
            return null;
        }
        return getOutputFile().getOrNull();
    }

    @Optional
    @OutputDirectory
    public java.io.File getMultiOutputDirectory() {
        if (GradleConfigAdapter.outputStyleOf(getOutputStyle().get()) == io.github.springpropertiesmd.core.config.OutputStyle.SINGLE_FILE) {
            return null;
        }
        return getOutputDirectory().getOrNull() != null ? getOutputDirectory().get().getAsFile() : null;
    }

    @Input
    public abstract Property<String> getTitle();

    @Input
    public abstract Property<String> getOutputStyle();

    @Input
    public abstract Property<String> getSensitiveMode();

    @Input
    public abstract Property<Boolean> getIncludeTableOfContents();

    @Input
    public abstract Property<Boolean> getIncludeDeprecated();

    @Input
    public abstract Property<Boolean> getIncludeValidation();

    @Input
    public abstract Property<Boolean> getIncludeExamples();

    @Input
    public abstract Property<Boolean> getIncludeCustomMetadata();

    @TaskAction
    public void generate() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(getClassesDir().get().getAsFile().toPath());

        if (bundle.groups().isEmpty() && bundle.properties().isEmpty()) {
            getLogger().info("No enriched metadata found, skipping documentation generation.");
            return;
        }

        GeneratorConfig config = generatorConfig();

        TableMarkdownGenerator generator = new TableMarkdownGenerator();
        RenderedDocumentation documentation = generator.render(bundle, config);
        new DocumentationFileWriter().write(documentation);

        for (Path output : documentation.files().keySet()) {
            getLogger().lifecycle("Generated documentation at: " + output);
        }
    }

    protected GeneratorConfig generatorConfig() {
        return new GeneratorConfig(
                getOutputFile().get().toPath(),
                getOutputDirectory().get().getAsFile().toPath(),
                getTitle().get(),
                GradleConfigAdapter.outputStyleOf(getOutputStyle().get()),
                getIncludeTableOfContents().get(),
                getIncludeDeprecated().get(),
                getIncludeValidation().get(),
                getIncludeExamples().get(),
                GradleConfigAdapter.sensitiveModeOf(getSensitiveMode().get()),
                getIncludeCustomMetadata().get()
        );
    }
}
