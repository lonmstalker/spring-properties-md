package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.ExampleValue;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
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

    @Override
    public RenderedDocumentation render(DocumentationBundle bundle, GeneratorConfig config) {
        return switch (config.outputStyle()) {
            case SINGLE_FILE -> RenderedDocumentation.ordered(Map.of(
                    config.outputFile(), generateDocument(bundle, config)
            ));
            case PER_GROUP -> renderPerGroup(bundle, config);
            case PER_CATEGORY -> renderPerCategory(bundle, config);
        };
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
                    config.includeExamples()
            ));
        } else {
            for (GroupMetadata group : sortedGroups) {
                List<PropertyMetadata> props = propertiesByGroup.getOrDefault(group.name(), List.of());
                if (props.isEmpty()) continue;

                sections.add(new MarkdownSection.GroupHeader(group.displayName(), group.description()));
                sections.add(new MarkdownSection.PropertyTable(
                        props,
                        config.includeValidation(),
                        config.includeExamples()
                ));
            }

            List<PropertyMetadata> ungrouped = propertiesByGroup.getOrDefault("", List.of());
            if (!ungrouped.isEmpty()) {
                sections.add(new MarkdownSection.GroupHeader("Other Properties", ""));
                sections.add(new MarkdownSection.PropertyTable(
                        ungrouped,
                        config.includeValidation(),
                        config.includeExamples()
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
