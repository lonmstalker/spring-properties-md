package io.github.springpropertiesmd.core.config;

import java.nio.file.Path;

public record ConditionConfig(
        boolean enabled,
        boolean springConditionalOnProperty,
        ExternalConditionMode externalConditionMode,
        Path externalConditionsOutputFile
) {
    public ConditionConfig {
        externalConditionMode = externalConditionMode != null ? externalConditionMode : ExternalConditionMode.WARN;
    }

    public static ConditionConfig defaults(Path externalConditionsOutputFile) {
        return new ConditionConfig(true, true, ExternalConditionMode.WARN, externalConditionsOutputFile);
    }

    public ConditionConfig withDefaults(Path defaultExternalConditionsOutputFile) {
        return new ConditionConfig(
                enabled,
                springConditionalOnProperty,
                externalConditionMode,
                externalConditionsOutputFile != null ? externalConditionsOutputFile : defaultExternalConditionsOutputFile
        );
    }

    public boolean renderMainConditions() {
        return enabled && springConditionalOnProperty;
    }
}
