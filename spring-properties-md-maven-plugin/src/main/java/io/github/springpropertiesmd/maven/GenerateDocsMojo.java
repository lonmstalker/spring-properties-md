package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class GenerateDocsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/configuration-properties.md")
    private String outputFile;

    @Parameter(defaultValue = "Configuration Properties")
    private String title;

    @Parameter(defaultValue = "SINGLE_FILE")
    private String outputStyle;

    @Parameter(defaultValue = "true")
    private boolean includeTableOfContents;

    @Parameter(defaultValue = "true")
    private boolean includeDeprecated;

    @Parameter(defaultValue = "true")
    private boolean includeValidation;

    @Parameter(defaultValue = "true")
    private boolean includeExamples;

    @Parameter(defaultValue = "true")
    private boolean includeSensitive;

    @Parameter(defaultValue = "false")
    private boolean includeCustomMetadata;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private String classesDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            Path classesDir = Path.of(classesDirectory);
            MetadataReader reader = new MetadataReader();
            DocumentationBundle bundle = reader.readFromClassesDir(classesDir);

            if (bundle.groups().isEmpty() && bundle.properties().isEmpty()) {
                getLog().info("No enriched metadata found, skipping documentation generation.");
                return;
            }

            MojoConfigAdapter adapter = new MojoConfigAdapter();
            GeneratorConfig config = adapter.adapt(
                    Path.of(outputFile), title, outputStyle,
                    includeTableOfContents, includeDeprecated, includeValidation,
                    includeExamples, includeSensitive, includeCustomMetadata
            );

            TableMarkdownGenerator generator = new TableMarkdownGenerator();
            String markdown = generator.generate(bundle, config);

            Path output = config.outputFile();
            Files.createDirectories(output.getParent());
            Files.writeString(output, markdown);

            getLog().info("Generated documentation at: " + output);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate documentation", e);
        }
    }
}
