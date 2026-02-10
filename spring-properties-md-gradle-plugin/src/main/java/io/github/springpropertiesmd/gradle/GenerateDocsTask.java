package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CacheableTask
public abstract class GenerateDocsTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getClassesDir();

    @OutputFile
    public abstract Property<java.io.File> getOutputFile();

    @Input
    public abstract Property<String> getTitle();

    @Input
    public abstract Property<String> getOutputStyle();

    @Input
    public abstract Property<Boolean> getIncludeTableOfContents();

    @Input
    public abstract Property<Boolean> getIncludeDeprecated();

    @Input
    public abstract Property<Boolean> getIncludeValidation();

    @Input
    public abstract Property<Boolean> getIncludeExamples();

    @Input
    public abstract Property<Boolean> getIncludeSensitive();

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

        Path outputPath = getOutputFile().get().toPath();

        GradleConfigAdapter adapter = new GradleConfigAdapter();
        SpringPropertiesMdExtension ext = getProject().getExtensions()
                .getByType(SpringPropertiesMdExtension.class);
        GeneratorConfig config = adapter.adapt(ext, outputPath);

        TableMarkdownGenerator generator = new TableMarkdownGenerator();
        String markdown = generator.generate(bundle, config);

        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, markdown);

        getLogger().lifecycle("Generated documentation at: " + outputPath);
    }
}
