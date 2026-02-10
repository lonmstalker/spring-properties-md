package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.core.config.GeneratorConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TableMarkdownGenerator implements MarkdownGenerator {

    private final MarkdownFormatter formatter = new MarkdownFormatter();

    @Override
    public String generate(DocumentationBundle bundle, GeneratorConfig config) {
        List<MarkdownSection> sections = new ArrayList<>();

        sections.add(new MarkdownSection.Title(config.title()));

        Map<String, GroupMetadata> groupMap = new LinkedHashMap<>();
        List<GroupMetadata> sortedGroups = bundle.groups().stream()
                .sorted(Comparator.comparingInt(GroupMetadata::order).thenComparing(GroupMetadata::name))
                .toList();
        for (GroupMetadata g : sortedGroups) {
            groupMap.put(g.name(), g);
        }

        Map<String, List<PropertyMetadata>> propertiesByGroup = new LinkedHashMap<>();
        for (PropertyMetadata p : bundle.properties()) {
            if (!config.includeDeprecated() && p.deprecation() != null) {
                continue;
            }
            String group = p.groupName() != null ? p.groupName() : "";
            propertiesByGroup.computeIfAbsent(group, k -> new ArrayList<>()).add(p);
        }

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
}
