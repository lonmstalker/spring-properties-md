package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.DocumentationFileWriter;
import io.github.springpropertiesmd.core.generator.RenderedDocumentation;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Path;

@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class GenerateDocsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/configuration-properties.md")
    private String outputFile;

    @Parameter(defaultValue = "${project.build.directory}/configuration-properties")
    private String outputDirectory;

    @Parameter(defaultValue = "Configuration Properties")
    private String title;

    @Parameter(defaultValue = "SINGLE_FILE")
    private String outputStyle;

    @Parameter(defaultValue = "REDACT")
    private String sensitiveMode;

    @Parameter(defaultValue = "true")
    private boolean includeTableOfContents;

    @Parameter(defaultValue = "true")
    private boolean includeDeprecated;

    @Parameter(defaultValue = "true")
    private boolean includeValidation;

    @Parameter(defaultValue = "true")
    private boolean includeExamples;

    @Parameter(defaultValue = "false")
    private boolean includeCustomMetadata;

    @Parameter
    private ConditionsMojoConfig conditions = new ConditionsMojoConfig();

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
                    Path.of(outputFile), Path.of(outputDirectory), title, outputStyle, sensitiveMode,
                    includeTableOfContents, includeDeprecated, includeValidation,
                    includeExamples, includeCustomMetadata, conditions
            );

            TableMarkdownGenerator generator = new TableMarkdownGenerator();
            RenderedDocumentation documentation = generator.render(bundle, config);
            new DocumentationFileWriter().write(documentation);

            for (Path output : documentation.files().keySet()) {
                getLog().info("Generated documentation at: " + output);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate documentation", e);
        }
    }
}
