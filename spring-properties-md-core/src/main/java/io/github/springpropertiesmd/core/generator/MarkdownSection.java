package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.PropertyMetadata;

import java.util.List;

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
                         boolean includeExamples) implements MarkdownSection {
    }

    record DeprecationNotice(String message) implements MarkdownSection {
    }

    record RawText(String text) implements MarkdownSection {
    }
}
