package io.github.springpropertiesmd.maven;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.check.CheckConfig;
import io.github.springpropertiesmd.core.check.DocumentationCheckResult;
import io.github.springpropertiesmd.core.check.DocumentationChecker;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Path;

@Mojo(name = "check-docs")
public class CheckDocsMojo extends AbstractMojo {

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

    @Parameter(defaultValue = "true")
    private boolean failOnMissingDescription;

    @Parameter(defaultValue = "true")
    private boolean failOnSensitiveDefault;

    @Parameter(defaultValue = "true")
    private boolean failOnDeprecatedWithoutReplacement;

    @Parameter(defaultValue = "true")
    private boolean failOnRequiredWithoutExample;

    @Parameter(defaultValue = "true")
    private boolean failOnDuplicatePropertyNames;

    @Parameter(defaultValue = "false")
    private boolean failIfGeneratedDocsChanged;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private String classesDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            MetadataReader reader = new MetadataReader();
            DocumentationBundle bundle = reader.readFromClassesDir(Path.of(classesDirectory));
            if (bundle.groups().isEmpty() && bundle.properties().isEmpty()) {
                getLog().info("No enriched metadata found, skipping documentation checks.");
                return;
            }

            GeneratorConfig generatorConfig = new MojoConfigAdapter().adapt(
                    Path.of(outputFile), Path.of(outputDirectory), title, outputStyle, sensitiveMode,
                    includeTableOfContents, includeDeprecated, includeValidation,
                    includeExamples, includeCustomMetadata
            );
            CheckConfig checkConfig = new CheckConfig(
                    failOnMissingDescription,
                    failOnSensitiveDefault,
                    failOnDeprecatedWithoutReplacement,
                    failOnRequiredWithoutExample,
                    failOnDuplicatePropertyNames,
                    failIfGeneratedDocsChanged
            );
            DocumentationCheckResult result = new DocumentationChecker(new TableMarkdownGenerator())
                    .check(bundle, generatorConfig, checkConfig);

            if (!result.passed()) {
                throw new MojoFailureException(result.format());
            }
            getLog().info(result.format());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to check documentation", e);
        }
    }
}
