package io.github.springpropertiesmd.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

import java.io.File;

public class SpringPropertiesMdPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        SpringPropertiesMdExtension extension = project.getExtensions()
                .create("springPropertiesMd", SpringPropertiesMdExtension.class);

        project.getTasks().register("generatePropertyDocs", GenerateDocsTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generate Markdown documentation from Spring Boot configuration properties");

            task.getTitle().convention(extension.getTitle());
            task.getOutputStyle().convention(extension.getOutputStyle());
            task.getSensitiveMode().convention(extension.getSensitiveMode());
            task.getIncludeTableOfContents().convention(extension.getIncludeTableOfContents());
            task.getIncludeDeprecated().convention(extension.getIncludeDeprecated());
            task.getIncludeValidation().convention(extension.getIncludeValidation());
            task.getIncludeExamples().convention(extension.getIncludeExamples());
            task.getIncludeCustomMetadata().convention(extension.getIncludeCustomMetadata());
            task.getConditionsEnabled().convention(extension.getConditions().getEnabled());
            task.getSpringConditionalOnProperty().convention(extension.getConditions().getSpringConditionalOnProperty());
            task.getExternalConditionMode().convention(extension.getConditions().getExternalConditionMode());
            task.getExternalConditionsOutputFile().convention(extension.getConditions().getExternalConditionsOutputFile());

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                task.dependsOn(project.getTasks().named("compileJava"));

                task.getClassesDir().convention(
                        project.getLayout().getBuildDirectory().dir("classes/java/main")
                );
            });

            task.getOutputFile().convention(
                    project.provider(() ->
                            new File(project.getLayout().getBuildDirectory().getAsFile().get(),
                                    "configuration-properties.md"))
            );
            task.getOutputDirectory().convention(
                    project.getLayout().getBuildDirectory().dir("configuration-properties")
            );

            if (extension.getOutputFile().isPresent()) {
                task.getOutputFile().set(new File(extension.getOutputFile().get()));
            }
            if (extension.getOutputDirectory().isPresent()) {
                task.getOutputDirectory().set(project.file(extension.getOutputDirectory().get()));
            }
        });

        project.getTasks().register("checkPropertyDocs", CheckDocsTask.class, task -> {
            task.setGroup("verification");
            task.setDescription("Check Spring Boot configuration property documentation quality");

            task.getTitle().convention(extension.getTitle());
            task.getOutputStyle().convention(extension.getOutputStyle());
            task.getSensitiveMode().convention(extension.getSensitiveMode());
            task.getIncludeTableOfContents().convention(extension.getIncludeTableOfContents());
            task.getIncludeDeprecated().convention(extension.getIncludeDeprecated());
            task.getIncludeValidation().convention(extension.getIncludeValidation());
            task.getIncludeExamples().convention(extension.getIncludeExamples());
            task.getIncludeCustomMetadata().convention(extension.getIncludeCustomMetadata());
            task.getConditionsEnabled().convention(extension.getConditions().getEnabled());
            task.getSpringConditionalOnProperty().convention(extension.getConditions().getSpringConditionalOnProperty());
            task.getExternalConditionMode().convention(extension.getConditions().getExternalConditionMode());
            task.getExternalConditionsOutputFile().convention(extension.getConditions().getExternalConditionsOutputFile());
            task.getFailOnMissingDescription().convention(extension.getFailOnMissingDescription());
            task.getFailOnSensitiveDefault().convention(extension.getFailOnSensitiveDefault());
            task.getFailOnDeprecatedWithoutReplacement().convention(extension.getFailOnDeprecatedWithoutReplacement());
            task.getFailOnRequiredWithoutExample().convention(extension.getFailOnRequiredWithoutExample());
            task.getFailOnDuplicatePropertyNames().convention(extension.getFailOnDuplicatePropertyNames());
            task.getFailIfGeneratedDocsChanged().convention(extension.getFailIfGeneratedDocsChanged());
            task.getFailOnUndocumentedLocalConditionProperty()
                    .convention(extension.getConditions().getChecks().getFailOnUndocumentedLocalConditionProperty());
            task.getWarnOnExternalConditionProperty()
                    .convention(extension.getConditions().getChecks().getWarnOnExternalConditionProperty());
            task.getWarnOnCollectionConditionProperty()
                    .convention(extension.getConditions().getChecks().getWarnOnCollectionConditionProperty());
            task.getWarnOnNonDashedConditionName()
                    .convention(extension.getConditions().getChecks().getWarnOnNonDashedConditionName());

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                task.dependsOn(project.getTasks().named("compileJava"));
                task.getClassesDir().convention(
                        project.getLayout().getBuildDirectory().dir("classes/java/main")
                );
            });

            task.getOutputFile().convention(
                    project.provider(() ->
                            new File(project.getLayout().getBuildDirectory().getAsFile().get(),
                                    "configuration-properties.md").toString())
            );
            task.getOutputDirectory().convention(
                    project.provider(() ->
                            new File(project.getLayout().getBuildDirectory().getAsFile().get(),
                                    "configuration-properties").toString())
            );

            if (extension.getOutputFile().isPresent()) {
                task.getOutputFile().set(extension.getOutputFile().get());
            }
            if (extension.getOutputDirectory().isPresent()) {
                task.getOutputDirectory().set(extension.getOutputDirectory().get());
            }
        });
    }
}
