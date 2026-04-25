package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.ConditionOwnerType;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyConditionMatchMode;
import io.github.springpropertiesmd.api.model.PropertyConditionMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.api.model.PropertyRequirement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ConditionExtractor {

    static final String CONFIGURATION_PROPERTIES =
            "org.springframework.boot.context.properties.ConfigurationProperties";
    static final String CONDITIONAL_ON_PROPERTY =
            "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty";
    static final String CONDITIONAL_ON_PROPERTIES =
            "org.springframework.boot.autoconfigure.condition.ConditionalOnProperties";

    private static final String ENABLE_CONFIGURATION_PROPERTIES =
            "org.springframework.boot.context.properties.EnableConfigurationProperties";

    private final ProcessingEnvironment processingEnv;
    private final List<PendingCondition> pendingConditions = new ArrayList<>();
    private final Map<String, String> configurationTypeGroups = new HashMap<>();
    private final Set<String> processedConditionElements = new HashSet<>();

    ConditionExtractor(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    void registerConfigurationType(TypeElement typeElement, String prefix) {
        configurationTypeGroups.put(typeElement.getQualifiedName().toString(), prefix);
    }

    void process(Element element) {
        String sourceElement = sourceElement(element);
        if (!processedConditionElements.add(sourceElement)) {
            return;
        }

        List<RawRequirement> requirements = extractConditionRequirements(element);
        if (requirements.isEmpty()) {
            return;
        }

        if (hasAnnotation(element, CONFIGURATION_PROPERTIES)) {
            pendingConditions.add(new PendingCondition(
                    sourceElement,
                    PendingOwnerKind.GROUP_PREFIX,
                    List.of(extractPrefix(element)),
                    requirements
            ));
            return;
        }

        List<String> enabledTypes = enableConfigurationPropertiesTypes(element);
        if (!enabledTypes.isEmpty()) {
            pendingConditions.add(new PendingCondition(
                    sourceElement,
                    PendingOwnerKind.ENABLE_CONFIGURATION_TYPES,
                    enabledTypes,
                    requirements
            ));
            return;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "@ConditionalOnProperty is ignored because spring-properties-md cannot map `"
                        + sourceElement + "` to a local @ConfigurationProperties group.",
                element);
    }

    List<PropertyConditionMetadata> classifiedConditions(List<GroupMetadata> groups,
                                                         List<PropertyMetadata> properties) {
        Set<String> localPropertyNames = new HashSet<>();
        for (PropertyMetadata property : properties) {
            if (property.name() != null) {
                localPropertyNames.add(property.name());
            }
        }

        Set<String> localPrefixes = new HashSet<>();
        for (GroupMetadata group : groups) {
            if (group.name() != null && !group.name().isBlank()) {
                localPrefixes.add(group.name());
            }
        }

        List<PropertyConditionMetadata> result = new ArrayList<>();
        for (PendingCondition pending : pendingConditions) {
            List<String> ownerIds = resolveOwnerIds(pending);
            if (ownerIds.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "@ConditionalOnProperty is ignored because spring-properties-md cannot map `"
                                + pending.sourceElement() + "` to a local @ConfigurationProperties group.");
                continue;
            }

            List<PropertyRequirement> requirements = pending.requirements().stream()
                    .map(requirement -> new PropertyRequirement(
                            requirement.propertyName(),
                            requirement.havingValue(),
                            requirement.matchIfMissing(),
                            requirement.matchMode(),
                            isLocal(requirement.propertyName(), localPropertyNames, localPrefixes)
                    ))
                    .toList();

            for (String ownerId : ownerIds) {
                result.add(new PropertyConditionMetadata(
                        pending.sourceElement(),
                        ownerId,
                        ConditionOwnerType.PROPERTY_GROUP,
                        requirements
                ));
            }
        }
        return result;
    }

    private List<String> resolveOwnerIds(PendingCondition pending) {
        if (pending.ownerKind() == PendingOwnerKind.GROUP_PREFIX) {
            return pending.ownerRefs();
        }
        return pending.ownerRefs().stream()
                .map(configurationTypeGroups::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private boolean isLocal(String propertyName, Set<String> localPropertyNames, Set<String> localPrefixes) {
        if (propertyName == null || propertyName.isBlank()) {
            return false;
        }
        if (localPropertyNames.contains(propertyName)) {
            return true;
        }
        return localPrefixes.stream()
                .anyMatch(prefix -> propertyName.equals(prefix) || propertyName.startsWith(prefix + "."));
    }

    private List<RawRequirement> extractConditionRequirements(Element element) {
        List<RawRequirement> requirements = new ArrayList<>();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            String annotationType = mirror.getAnnotationType().toString();
            if (CONDITIONAL_ON_PROPERTY.equals(annotationType)) {
                requirements.addAll(extractConditionRequirements(mirror));
            } else if (CONDITIONAL_ON_PROPERTIES.equals(annotationType)) {
                requirements.addAll(extractConditionRequirementsFromContainer(mirror));
            }
        }
        return requirements;
    }

    private List<RawRequirement> extractConditionRequirementsFromContainer(AnnotationMirror container) {
        AnnotationValue value = annotationValue(container, "value");
        if (value == null || !(value.getValue() instanceof List<?> nestedValues)) {
            return List.of();
        }

        List<RawRequirement> requirements = new ArrayList<>();
        for (Object nestedValue : nestedValues) {
            if (nestedValue instanceof AnnotationValue annotationValue
                    && annotationValue.getValue() instanceof AnnotationMirror nestedMirror) {
                requirements.addAll(extractConditionRequirements(nestedMirror));
            }
        }
        return requirements;
    }

    private List<RawRequirement> extractConditionRequirements(AnnotationMirror mirror) {
        String prefix = stringValue(mirror, "prefix", "");
        List<String> names = stringArrayValue(mirror, "name");
        if (names.isEmpty()) {
            names = stringArrayValue(mirror, "value");
        }
        String havingValue = stringValue(mirror, "havingValue", "");
        boolean matchIfMissing = booleanValue(mirror, "matchIfMissing", false);
        PropertyConditionMatchMode matchMode = havingValue == null || havingValue.isBlank()
                ? PropertyConditionMatchMode.PRESENT_AND_NOT_FALSE
                : PropertyConditionMatchMode.EQUALS_VALUE;

        List<RawRequirement> requirements = new ArrayList<>();
        for (String name : names) {
            String propertyName = normalizePropertyName(prefix, name);
            if (!propertyName.isBlank()) {
                requirements.add(new RawRequirement(propertyName, havingValue, matchIfMissing, matchMode));
            }
        }
        return requirements;
    }

    private List<String> enableConfigurationPropertiesTypes(Element element) {
        AnnotationMirror annotation = element.getAnnotationMirrors().stream()
                .filter(mirror -> ENABLE_CONFIGURATION_PROPERTIES.equals(mirror.getAnnotationType().toString()))
                .findFirst()
                .orElse(null);
        if (annotation == null) {
            return List.of();
        }
        return stringArrayValue(annotation, "value");
    }

    private boolean hasAnnotation(Element element, String annotationName) {
        return element.getAnnotationMirrors().stream()
                .anyMatch(mirror -> annotationName.equals(mirror.getAnnotationType().toString()));
    }

    private String extractPrefix(Element element) {
        var annotation = element.getAnnotationMirrors().stream()
                .filter(am -> CONFIGURATION_PROPERTIES.equals(am.getAnnotationType().toString()))
                .findFirst()
                .orElse(null);
        if (annotation == null) {
            return "";
        }
        for (var entry : annotation.getElementValues().entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            if ("prefix".equals(key) || "value".equals(key)) {
                return entry.getValue().getValue().toString();
            }
        }
        return "";
    }

    private AnnotationValue annotationValue(AnnotationMirror mirror, String name) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (name.equals(entry.getKey().getSimpleName().toString())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String stringValue(AnnotationMirror mirror, String name, String defaultValue) {
        AnnotationValue value = annotationValue(mirror, name);
        return value != null && value.getValue() != null ? value.getValue().toString() : defaultValue;
    }

    private boolean booleanValue(AnnotationMirror mirror, String name, boolean defaultValue) {
        AnnotationValue value = annotationValue(mirror, name);
        return value != null && value.getValue() instanceof Boolean bool ? bool : defaultValue;
    }

    private List<String> stringArrayValue(AnnotationMirror mirror, String name) {
        AnnotationValue value = annotationValue(mirror, name);
        if (value == null || value.getValue() == null) {
            return List.of();
        }
        if (value.getValue() instanceof List<?> values) {
            return values.stream()
                    .filter(AnnotationValue.class::isInstance)
                    .map(AnnotationValue.class::cast)
                    .map(annotationValue -> annotationValue.getValue().toString())
                    .toList();
        }
        return List.of(value.getValue().toString());
    }

    private String normalizePropertyName(String prefix, String name) {
        String cleanPrefix = prefix == null ? "" : prefix.strip();
        String cleanName = name == null ? "" : name.strip();
        if (cleanPrefix.isEmpty()) {
            return cleanName;
        }
        if (cleanName.isEmpty()) {
            return cleanPrefix;
        }
        return cleanPrefix.endsWith(".") ? cleanPrefix + cleanName : cleanPrefix + "." + cleanName;
    }

    private String sourceElement(Element element) {
        if (element instanceof TypeElement typeElement) {
            return typeElement.getQualifiedName().toString();
        }
        if (element instanceof ExecutableElement method
                && method.getEnclosingElement() instanceof TypeElement owner) {
            return owner.getQualifiedName() + "#" + method.getSimpleName();
        }
        return element.toString();
    }

    private enum PendingOwnerKind {
        GROUP_PREFIX,
        ENABLE_CONFIGURATION_TYPES
    }

    private record RawRequirement(
            String propertyName,
            String havingValue,
            boolean matchIfMissing,
            PropertyConditionMatchMode matchMode
    ) {
    }

    private record PendingCondition(
            String sourceElement,
            PendingOwnerKind ownerKind,
            List<String> ownerRefs,
            List<RawRequirement> requirements
    ) {
    }
}
