package io.github.springpropertiesmd.gradle;

import org.gradle.api.provider.Property;

public abstract class SpringPropertiesMdExtension {

    public abstract Property<String> getOutputFile();

    public abstract Property<String> getOutputDirectory();

    public abstract Property<String> getTitle();

    public abstract Property<String> getOutputStyle();

    public abstract Property<String> getSensitiveMode();

    public abstract Property<Boolean> getIncludeTableOfContents();

    public abstract Property<Boolean> getIncludeDeprecated();

    public abstract Property<Boolean> getIncludeValidation();

    public abstract Property<Boolean> getIncludeExamples();

    public abstract Property<Boolean> getIncludeCustomMetadata();

    public abstract Property<Boolean> getFailOnMissingDescription();

    public abstract Property<Boolean> getFailOnSensitiveDefault();

    public abstract Property<Boolean> getFailOnDeprecatedWithoutReplacement();

    public abstract Property<Boolean> getFailOnRequiredWithoutExample();

    public abstract Property<Boolean> getFailOnDuplicatePropertyNames();

    public abstract Property<Boolean> getFailIfGeneratedDocsChanged();

    public SpringPropertiesMdExtension() {
        getTitle().convention("Configuration Properties");
        getOutputStyle().convention("SINGLE_FILE");
        getSensitiveMode().convention("REDACT");
        getIncludeTableOfContents().convention(true);
        getIncludeDeprecated().convention(true);
        getIncludeValidation().convention(true);
        getIncludeExamples().convention(true);
        getIncludeCustomMetadata().convention(false);
        getFailOnMissingDescription().convention(true);
        getFailOnSensitiveDefault().convention(true);
        getFailOnDeprecatedWithoutReplacement().convention(true);
        getFailOnRequiredWithoutExample().convention(true);
        getFailOnDuplicatePropertyNames().convention(true);
        getFailIfGeneratedDocsChanged().convention(false);
    }
}
