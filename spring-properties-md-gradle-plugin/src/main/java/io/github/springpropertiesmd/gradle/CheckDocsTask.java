package io.github.springpropertiesmd.gradle;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.check.CheckConfig;
import io.github.springpropertiesmd.core.check.DocumentationCheckResult;
import io.github.springpropertiesmd.core.check.DocumentationChecker;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.gradle.api.GradleException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class CheckDocsTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getClassesDir();

    @Input
    public abstract Property<String> getOutputFile();

    @Input
    public abstract Property<String> getOutputDirectory();

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

    @Input
    public abstract Property<Boolean> getFailOnMissingDescription();

    @Input
    public abstract Property<Boolean> getFailOnSensitiveDefault();

    @Input
    public abstract Property<Boolean> getFailOnDeprecatedWithoutReplacement();

    @Input
    public abstract Property<Boolean> getFailOnRequiredWithoutExample();

    @Input
    public abstract Property<Boolean> getFailOnDuplicatePropertyNames();

    @Input
    public abstract Property<Boolean> getFailIfGeneratedDocsChanged();

    @Input
    public abstract Property<Boolean> getConditionsEnabled();

    @Input
    public abstract Property<Boolean> getSpringConditionalOnProperty();

    @Input
    public abstract Property<String> getExternalConditionMode();

    @Input
    @Optional
    public abstract Property<String> getExternalConditionsOutputFile();

    @Input
    public abstract Property<Boolean> getFailOnUndocumentedLocalConditionProperty();

    @Input
    public abstract Property<Boolean> getWarnOnExternalConditionProperty();

    @Input
    public abstract Property<Boolean> getWarnOnCollectionConditionProperty();

    @Input
    public abstract Property<Boolean> getWarnOnNonDashedConditionName();

    @TaskAction
    public void check() throws IOException {
        DocumentationBundle bundle = new MetadataReader()
                .readFromClassesDir(getClassesDir().get().getAsFile().toPath());
        if (bundle.groups().isEmpty() && bundle.properties().isEmpty()) {
            getLogger().info("No enriched metadata found, skipping documentation checks.");
            return;
        }

        DocumentationCheckResult result = new DocumentationChecker(new TableMarkdownGenerator())
                .check(bundle, generatorConfig(), checkConfig());
        if (!result.passed()) {
            throw new GradleException(result.format());
        }
        getLogger().lifecycle(result.format());
    }

    private GeneratorConfig generatorConfig() {
        return new GeneratorConfig(
                java.nio.file.Path.of(getOutputFile().get()),
                java.nio.file.Path.of(getOutputDirectory().get()),
                getTitle().get(),
                GradleConfigAdapter.outputStyleOf(getOutputStyle().get()),
                getIncludeTableOfContents().get(),
                getIncludeDeprecated().get(),
                getIncludeValidation().get(),
                getIncludeExamples().get(),
                GradleConfigAdapter.sensitiveModeOf(getSensitiveMode().get()),
                getIncludeCustomMetadata().get(),
                new io.github.springpropertiesmd.core.config.ConditionConfig(
                        getConditionsEnabled().get(),
                        getSpringConditionalOnProperty().get(),
                        GradleConfigAdapter.externalConditionModeOf(getExternalConditionMode().get()),
                        getExternalConditionsOutputFile().isPresent()
                                ? java.nio.file.Path.of(getExternalConditionsOutputFile().get())
                                : null
                )
        );
    }

    private CheckConfig checkConfig() {
        return new CheckConfig(
                getFailOnMissingDescription().get(),
                getFailOnSensitiveDefault().get(),
                getFailOnDeprecatedWithoutReplacement().get(),
                getFailOnRequiredWithoutExample().get(),
                getFailOnDuplicatePropertyNames().get(),
                getFailIfGeneratedDocsChanged().get(),
                new io.github.springpropertiesmd.core.check.ConditionCheckConfig(
                        getFailOnUndocumentedLocalConditionProperty().get(),
                        getWarnOnExternalConditionProperty().get(),
                        getWarnOnCollectionConditionProperty().get(),
                        getWarnOnNonDashedConditionName().get()
                )
        );
    }
}
