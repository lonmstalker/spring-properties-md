package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.PropertyConditionMatchMode;
import io.github.springpropertiesmd.api.model.PropertyConditionMetadata;
import io.github.springpropertiesmd.api.model.PropertyRequirement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class ConditionTextFormatter {

    String bulletList(List<PropertyConditionMetadata> conditions) {
        return conditions.stream()
                .flatMap(condition -> condition.requirements().stream())
                .map(this::sentence)
                .distinct()
                .map(sentence -> "- " + sentence)
                .collect(Collectors.joining("\n"));
    }

    String inline(List<PropertyConditionMetadata> conditions) {
        List<String> requirements = conditions.stream()
                .flatMap(condition -> condition.requirements().stream())
                .map(this::sentence)
                .distinct()
                .toList();
        if (requirements.isEmpty()) {
            return "";
        }
        return String.join("; ", requirements);
    }

    String externalDocument(List<PropertyConditionMetadata> conditions) {
        StringBuilder sb = new StringBuilder();
        sb.append("# External Property Conditions\n\n");
        sb.append("These properties are referenced by `@ConditionalOnProperty` in this project, ");
        sb.append("but are not documented as project configuration properties.\n");

        for (var entry : externalRequirementsByProperty(conditions).entrySet()) {
            sb.append("\n## `").append(entry.getKey()).append("`\n\n");
            sb.append("Used by:\n\n");
            entry.getValue().stream()
                    .map(use -> use.sourceElement() + " -> " + use.ownerId())
                    .distinct()
                    .forEach(source -> sb.append("- `").append(source).append("`\n"));
            sb.append("\nCondition:\n\n");
            entry.getValue().stream()
                    .map(ExternalRequirementUse::requirement)
                    .map(this::sentence)
                    .distinct()
                    .forEach(sentence -> sb.append("- ").append(sentence).append("\n"));
        }
        return sb.toString().stripTrailing() + "\n";
    }

    private java.util.Map<String, List<ExternalRequirementUse>> externalRequirementsByProperty(
            List<PropertyConditionMetadata> conditions
    ) {
        java.util.Map<String, List<ExternalRequirementUse>> result = new java.util.LinkedHashMap<>();
        for (PropertyConditionMetadata condition : conditions) {
            for (PropertyRequirement requirement : condition.requirements()) {
                if (!requirement.local()) {
                    result.computeIfAbsent(requirement.propertyName(), ignored -> new ArrayList<>())
                            .add(new ExternalRequirementUse(condition.sourceElement(), condition.ownerId(), requirement));
                }
            }
        }
        return result;
    }

    private String sentence(PropertyRequirement requirement) {
        String base = requirement.matchMode() == PropertyConditionMatchMode.EQUALS_VALUE
                ? "`" + requirement.propertyName() + "=" + requirement.havingValue() + "`"
                : "`" + requirement.propertyName() + "` must be present and not equal to `false`";
        if (requirement.matchIfMissing()) {
            return base + ", or the property is missing";
        }
        return base;
    }

    private record ExternalRequirementUse(String sourceElement, String ownerId, PropertyRequirement requirement) {
    }
}
