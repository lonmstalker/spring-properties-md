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
            task.getIncludeTableOfContents().convention(extension.getIncludeTableOfContents());
            task.getIncludeDeprecated().convention(extension.getIncludeDeprecated());
            task.getIncludeValidation().convention(extension.getIncludeValidation());
            task.getIncludeExamples().convention(extension.getIncludeExamples());
            task.getIncludeSensitive().convention(extension.getIncludeSensitive());
            task.getIncludeCustomMetadata().convention(extension.getIncludeCustomMetadata());

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

            if (extension.getOutputFile().isPresent()) {
                task.getOutputFile().set(new File(extension.getOutputFile().get()));
            }
        });
    }
}
