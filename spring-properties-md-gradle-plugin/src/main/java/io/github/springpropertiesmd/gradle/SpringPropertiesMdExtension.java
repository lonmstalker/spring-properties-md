package io.github.springpropertiesmd.gradle;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class SpringPropertiesMdExtension {

    private final ConditionsExtension conditions;

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

    @Inject
    public SpringPropertiesMdExtension(ObjectFactory objects) {
        this.conditions = objects.newInstance(ConditionsExtension.class);
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

    public ConditionsExtension getConditions() {
        return conditions;
    }

    public void conditions(Action<? super ConditionsExtension> action) {
        action.execute(conditions);
    }

    public abstract static class ConditionsExtension {

        private final ConditionChecksExtension checks;

        public abstract Property<Boolean> getEnabled();

        public abstract Property<Boolean> getSpringConditionalOnProperty();

        public abstract Property<String> getExternalConditionMode();

        public abstract Property<String> getExternalConditionsOutputFile();

        @Inject
        public ConditionsExtension(ObjectFactory objects) {
            this.checks = objects.newInstance(ConditionChecksExtension.class);
            getEnabled().convention(true);
            getSpringConditionalOnProperty().convention(true);
            getExternalConditionMode().convention("WARN");
        }

        public ConditionChecksExtension getChecks() {
            return checks;
        }

        public void checks(Action<? super ConditionChecksExtension> action) {
            action.execute(checks);
        }
    }

    public abstract static class ConditionChecksExtension {

        public abstract Property<Boolean> getFailOnUndocumentedLocalConditionProperty();

        public abstract Property<Boolean> getWarnOnExternalConditionProperty();

        public abstract Property<Boolean> getWarnOnCollectionConditionProperty();

        public abstract Property<Boolean> getWarnOnNonDashedConditionName();

        public ConditionChecksExtension() {
            getFailOnUndocumentedLocalConditionProperty().convention(true);
            getWarnOnExternalConditionProperty().convention(true);
            getWarnOnCollectionConditionProperty().convention(true);
            getWarnOnNonDashedConditionName().convention(true);
        }
    }
}
