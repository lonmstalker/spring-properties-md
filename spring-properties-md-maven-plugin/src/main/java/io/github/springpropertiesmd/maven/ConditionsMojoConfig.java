package io.github.springpropertiesmd.maven;

import org.apache.maven.plugins.annotations.Parameter;

public class ConditionsMojoConfig {

    @Parameter(defaultValue = "true")
    private boolean enabled = true;

    @Parameter(defaultValue = "true")
    private boolean springConditionalOnProperty = true;

    @Parameter(defaultValue = "WARN")
    private String externalConditionMode = "WARN";

    @Parameter
    private String externalConditionsOutputFile;

    @Parameter
    private Checks checks = new Checks();

    public boolean enabled() {
        return enabled;
    }

    public boolean springConditionalOnProperty() {
        return springConditionalOnProperty;
    }

    public String externalConditionMode() {
        return externalConditionMode;
    }

    public String externalConditionsOutputFile() {
        return externalConditionsOutputFile;
    }

    public Checks checks() {
        return checks != null ? checks : new Checks();
    }

    public static class Checks {

        @Parameter(defaultValue = "true")
        private boolean failOnUndocumentedLocalConditionProperty = true;

        @Parameter(defaultValue = "true")
        private boolean warnOnExternalConditionProperty = true;

        @Parameter(defaultValue = "true")
        private boolean warnOnCollectionConditionProperty = true;

        @Parameter(defaultValue = "true")
        private boolean warnOnNonDashedConditionName = true;

        public boolean failOnUndocumentedLocalConditionProperty() {
            return failOnUndocumentedLocalConditionProperty;
        }

        public boolean warnOnExternalConditionProperty() {
            return warnOnExternalConditionProperty;
        }

        public boolean warnOnCollectionConditionProperty() {
            return warnOnCollectionConditionProperty;
        }

        public boolean warnOnNonDashedConditionName() {
            return warnOnNonDashedConditionName;
        }
    }
}
