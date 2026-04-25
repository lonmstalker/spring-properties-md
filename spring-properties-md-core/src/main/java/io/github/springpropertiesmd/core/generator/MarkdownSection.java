package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.PropertyConditionMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;

import java.util.List;
import java.util.Map;

public sealed interface MarkdownSection {

    record Title(String text) implements MarkdownSection {
    }

    record TableOfContents(List<TocEntry> entries) implements MarkdownSection {
        public record TocEntry(String displayName, String anchor) {
        }
    }

    record GroupHeader(String displayName, String description) implements MarkdownSection {
    }

    record PropertyTable(List<PropertyMetadata> properties, boolean includeValidation,
                         boolean includeExamples,
                         Map<String, List<PropertyConditionMetadata>> conditionsByProperty) implements MarkdownSection {
        public PropertyTable(List<PropertyMetadata> properties, boolean includeValidation, boolean includeExamples) {
            this(properties, includeValidation, includeExamples, Map.of());
        }

        public PropertyTable {
            conditionsByProperty = conditionsByProperty == null ? Map.of() : Map.copyOf(conditionsByProperty);
        }
    }

    record DeprecationNotice(String message) implements MarkdownSection {
    }

    record RawText(String text) implements MarkdownSection {
    }
}
