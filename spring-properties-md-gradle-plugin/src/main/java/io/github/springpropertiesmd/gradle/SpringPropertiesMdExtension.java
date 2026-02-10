package io.github.springpropertiesmd.gradle;

import org.gradle.api.provider.Property;

public abstract class SpringPropertiesMdExtension {

    public abstract Property<String> getOutputFile();

    public abstract Property<String> getTitle();

    public abstract Property<String> getOutputStyle();

    public abstract Property<Boolean> getIncludeTableOfContents();

    public abstract Property<Boolean> getIncludeDeprecated();

    public abstract Property<Boolean> getIncludeValidation();

    public abstract Property<Boolean> getIncludeExamples();

    public abstract Property<Boolean> getIncludeSensitive();

    public abstract Property<Boolean> getIncludeCustomMetadata();

    public SpringPropertiesMdExtension() {
        getTitle().convention("Configuration Properties");
        getOutputStyle().convention("SINGLE_FILE");
        getIncludeTableOfContents().convention(true);
        getIncludeDeprecated().convention(true);
        getIncludeValidation().convention(true);
        getIncludeExamples().convention(true);
        getIncludeSensitive().convention(true);
        getIncludeCustomMetadata().convention(false);
    }
}
