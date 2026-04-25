package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.ExampleValue;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.ConditionOwnerType;
import io.github.springpropertiesmd.api.model.PropertyConditionMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.core.config.ExternalConditionMode;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TableMarkdownGenerator implements MarkdownGenerator {

    private final MarkdownFormatter formatter = new MarkdownFormatter();
    private final ConditionTextFormatter conditionFormatter = new ConditionTextFormatter();

    @Override
    public RenderedDocumentation render(DocumentationBundle bundle, GeneratorConfig config) {
        RenderedDocumentation mainDocumentation = switch (config.outputStyle()) {
            case SINGLE_FILE -> {
                Map<Path, String> files = new LinkedHashMap<>();
                files.put(config.outputFile(), generateDocument(bundle, config));
                yield RenderedDocumentation.ordered(files);
            }
            case PER_GROUP -> renderPerGroup(bundle, config);
            case PER_CATEGORY -> renderPerCategory(bundle, config);
        };
        return withExternalConditions(mainDocumentation, bundle, config);
    }

    private RenderedDocumentation renderPerGroup(DocumentationBundle bundle, GeneratorConfig config) {
        Map<String, List<PropertyMetadata>> propertiesByGroup = propertiesByGroup(bundle, config);
        Map<Path, String> files = new LinkedHashMap<>();

        for (GroupMetadata group : sortedGroups(bundle.groups())) {
            List<PropertyMetadata> props = propertiesByGroup.getOrDefault(group.name(), List.of());
            if (!props.isEmpty()) {
                files.put(config.outputDirectory().resolve(sanitizeFileName(groupFileName(group)) + ".md"),
                        generateDocument(new DocumentationBundle(List.of(group), props), config));
            }
        }

        List<PropertyMetadata> ungrouped = propertiesByGroup.getOrDefault("", List.of());
        if (!ungrouped.isEmpty()) {
            files.put(config.outputDirectory().resolve("other-properties.md"),
                    generateDocument(new DocumentationBundle(List.of(), ungrouped), config));
        }

        return RenderedDocumentation.ordered(files);
    }

    private RenderedDocumentation renderPerCategory(DocumentationBundle bundle, GeneratorConfig config) {
        Map<String, List<PropertyMetadata>> propertiesByGroup = propertiesByGroup(bundle, config);
        Map<String, List<GroupMetadata>> groupsByCategory = new LinkedHashMap<>();
        Map<String, List<PropertyMetadata>> ungroupedByCategory = new LinkedHashMap<>();

        for (GroupMetadata group : sortedGroups(bundle.groups())) {
            List<PropertyMetadata> props = propertiesByGroup.getOrDefault(group.name(), List.of());
            if (!props.isEmpty()) {
                groupsByCategory.computeIfAbsent(categoryOf(group), ignored -> new ArrayList<>()).add(group);
            }
        }

        for (PropertyMetadata property : propertiesByGroup.getOrDefault("", List.of())) {
            String category = isBlank(property.category()) ? "Uncategorized" : property.category();
            ungroupedByCategory.computeIfAbsent(category, ignored -> new ArrayList<>()).add(property);
        }

        Map<Path, String> files = new LinkedHashMap<>();
        for (var entry : groupsByCategory.entrySet()) {
            List<String> groupNames = entry.getValue().stream().map(GroupMetadata::name).toList();
            List<PropertyMetadata> props = new ArrayList<>();
            for (String groupName : groupNames) {
                props.addAll(propertiesByGroup.getOrDefault(groupName, List.of()));
            }
            props.addAll(ungroupedByCategory.getOrDefault(entry.getKey(), List.of()));
            files.put(config.outputDirectory().resolve(sanitizeFileName(entry.getKey()) + ".md"),
                    generateDocument(new DocumentationBundle(entry.getValue(), props), config));
        }

        for (var entry : ungroupedByCategory.entrySet()) {
            if (!files.containsKey(config.outputDirectory().resolve(sanitizeFileName(entry.getKey()) + ".md"))) {
                files.put(config.outputDirectory().resolve(sanitizeFileName(entry.getKey()) + ".md"),
                        generateDocument(new DocumentationBundle(List.of(), entry.getValue()), config));
            }
        }

        return RenderedDocumentation.ordered(files);
    }

    private String generateDocument(DocumentationBundle bundle, GeneratorConfig config) {
        List<MarkdownSection> sections = new ArrayList<>();

        sections.add(new MarkdownSection.Title(config.title()));

        Map<String, GroupMetadata> groupMap = groupsByName(bundle);
        List<GroupMetadata> sortedGroups = sortedGroups(bundle.groups());
        for (GroupMetadata g : sortedGroups) {
            groupMap.put(g.name(), g);
        }

        Map<String, List<PropertyMetadata>> propertiesByGroup = propertiesByGroup(bundle, config);
        Map<String, List<PropertyConditionMetadata>> groupConditions = conditionsByOwner(
                bundle, ConditionOwnerType.PROPERTY_GROUP, config);
        Map<String, List<PropertyConditionMetadata>> propertyConditions = conditionsByOwner(
                bundle, ConditionOwnerType.PROPERTY, config);

        if (config.includeTableOfContents() && !groupMap.isEmpty()) {
            List<MarkdownSection.TableOfContents.TocEntry> tocEntries = new ArrayList<>();
            for (GroupMetadata g : sortedGroups) {
                tocEntries.add(new MarkdownSection.TableOfContents.TocEntry(
                        g.displayName(),
                        MarkdownFormatter.toAnchor(g.displayName())
                ));
            }
            sections.add(new MarkdownSection.TableOfContents(tocEntries));
            sections.add(new MarkdownSection.RawText("\n---"));
        }

        if (groupMap.isEmpty() && !bundle.properties().isEmpty()) {
            sections.add(new MarkdownSection.PropertyTable(
                    bundle.properties(),
                    config.includeValidation(),
                    config.includeExamples(),
                    effectiveConditionsByProperty(bundle.properties(), groupConditions, propertyConditions)
            ));
        } else {
            for (GroupMetadata group : sortedGroups) {
                List<PropertyMetadata> props = propertiesByGroup.getOrDefault(group.name(), List.of());
                if (props.isEmpty()) continue;

                sections.add(new MarkdownSection.GroupHeader(group.displayName(), group.description()));
                addGroupConditions(sections, groupConditions.getOrDefault(group.name(), List.of()));
                sections.add(new MarkdownSection.PropertyTable(
                        props,
                        config.includeValidation(),
                        config.includeExamples(),
                        effectiveConditionsByProperty(props, groupConditions, propertyConditions)
                ));
            }

            List<PropertyMetadata> ungrouped = propertiesByGroup.getOrDefault("", List.of());
            if (!ungrouped.isEmpty()) {
                sections.add(new MarkdownSection.GroupHeader("Other Properties", ""));
                sections.add(new MarkdownSection.PropertyTable(
                        ungrouped,
                        config.includeValidation(),
                        config.includeExamples(),
                        effectiveConditionsByProperty(ungrouped, groupConditions, propertyConditions)
                ));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (MarkdownSection section : sections) {
            sb.append(formatter.render(section));
            sb.append("\n");
        }

        return sb.toString().stripTrailing() + "\n";
    }

    private void addGroupConditions(List<MarkdownSection> sections, List<PropertyConditionMetadata> conditions) {
        if (conditions.isEmpty()) {
            return;
        }
        sections.add(new MarkdownSection.RawText("\nApplies when:\n\n" + conditionFormatter.bulletList(conditions)));
    }

    private RenderedDocumentation withExternalConditions(RenderedDocumentation documentation, DocumentationBundle bundle,
                                                         GeneratorConfig config) {
        if (!config.conditions().enabled()
                || !config.conditions().springConditionalOnProperty()
                || config.conditions().externalConditionMode() != ExternalConditionMode.SEPARATE_FILE) {
            return documentation;
        }
        List<PropertyConditionMetadata> externalConditions = bundle.conditions().stream()
                .filter(condition -> condition.requirements().stream().anyMatch(requirement -> !requirement.local()))
                .toList();
        if (externalConditions.isEmpty()) {
            return documentation;
        }
        Map<Path, String> files = new LinkedHashMap<>(documentation.files());
        files.put(config.conditions().externalConditionsOutputFile(),
                conditionFormatter.externalDocument(externalConditions));
        return RenderedDocumentation.ordered(files);
    }

    private Map<String, List<PropertyConditionMetadata>> conditionsByOwner(
            DocumentationBundle bundle,
            ConditionOwnerType ownerType,
            GeneratorConfig config
    ) {
        if (!config.conditions().renderMainConditions()) {
            return Map.of();
        }
        Map<String, List<PropertyConditionMetadata>> result = new LinkedHashMap<>();
        for (PropertyConditionMetadata condition : bundle.conditions()) {
            if (condition.ownerType() != ownerType || condition.requirements().isEmpty()) {
                continue;
            }
            if (condition.requirements().stream().anyMatch(requirement -> !requirement.local())) {
                continue;
            }
            result.computeIfAbsent(condition.ownerId(), ignored -> new ArrayList<>()).add(condition);
        }
        return result;
    }

    private Map<String, List<PropertyConditionMetadata>> effectiveConditionsByProperty(
            List<PropertyMetadata> properties,
            Map<String, List<PropertyConditionMetadata>> groupConditions,
            Map<String, List<PropertyConditionMetadata>> propertyConditions
    ) {
        Map<String, List<PropertyConditionMetadata>> result = new LinkedHashMap<>();
        for (PropertyMetadata property : properties) {
            List<PropertyConditionMetadata> conditions = new ArrayList<>();
            conditions.addAll(groupConditions.getOrDefault(property.groupName(), List.of()));
            conditions.addAll(propertyConditions.getOrDefault(property.name(), List.of()));
            if (!conditions.isEmpty()) {
                result.put(property.name(), conditions);
            }
        }
        return result;
    }

    private Map<String, GroupMetadata> groupsByName(DocumentationBundle bundle) {
        Map<String, GroupMetadata> groupMap = new LinkedHashMap<>();
        for (GroupMetadata group : sortedGroups(bundle.groups())) {
            groupMap.put(group.name(), group);
        }
        return groupMap;
    }

    private List<GroupMetadata> sortedGroups(List<GroupMetadata> groups) {
        return groups.stream()
                .sorted(Comparator.comparingInt(GroupMetadata::order).thenComparing(GroupMetadata::name))
                .toList();
    }

    private Map<String, List<PropertyMetadata>> propertiesByGroup(DocumentationBundle bundle, GeneratorConfig config) {
        Map<String, List<PropertyMetadata>> propertiesByGroup = new LinkedHashMap<>();
        for (PropertyMetadata p : bundle.properties()) {
            if (!config.includeDeprecated() && p.deprecation() != null) {
                continue;
            }
            PropertyMetadata renderProperty = renderProperty(p, config.sensitiveMode());
            if (renderProperty == null) {
                continue;
            }
            String group = renderProperty.groupName() != null ? renderProperty.groupName() : "";
            propertiesByGroup.computeIfAbsent(group, k -> new ArrayList<>()).add(renderProperty);
        }
        return propertiesByGroup;
    }

    private PropertyMetadata renderProperty(PropertyMetadata property, SensitiveMode sensitiveMode) {
        if (!property.sensitive()) {
            return property;
        }
        if (sensitiveMode == SensitiveMode.OMIT) {
            return null;
        }
        if (sensitiveMode == SensitiveMode.SHOW) {
            return property;
        }

        String defaultValue = isBlank(property.defaultValue()) ? property.defaultValue() : "***";
        List<ExampleValue> examples = property.examples().stream()
                .map(example -> new ExampleValue("***", example.description()))
                .toList();
        return new PropertyMetadata(
                property.name(), property.type(), property.typeDisplay(), property.description(),
                defaultValue, property.required(), property.sensitive(), property.profiles(),
                property.deprecation(), examples, property.constraints(), property.category(),
                property.subcategory(), property.since(), property.seeAlso(), property.customMetadata(),
                property.sourceType(), property.groupName()
        );
    }

    private String categoryOf(GroupMetadata group) {
        return isBlank(group.category()) ? "Uncategorized" : group.category();
    }

    private String groupFileName(GroupMetadata group) {
        return isBlank(group.name()) ? group.displayName() : group.name();
    }

    private String sanitizeFileName(String input) {
        String sanitized = input == null ? "" : input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return sanitized.isEmpty() ? "properties" : sanitized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
