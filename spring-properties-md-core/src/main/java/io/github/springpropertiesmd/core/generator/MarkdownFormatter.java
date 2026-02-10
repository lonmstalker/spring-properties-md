package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.ExampleValue;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.api.model.ValidationConstraint;

import java.util.List;
import java.util.stream.Collectors;

public class MarkdownFormatter {

    public String render(MarkdownSection section) {
        return switch (section) {
            case MarkdownSection.Title t -> "# " + t.text() + "\n";
            case MarkdownSection.TableOfContents toc -> renderToc(toc);
            case MarkdownSection.GroupHeader g -> renderGroupHeader(g);
            case MarkdownSection.PropertyTable t -> renderPropertyTable(t);
            case MarkdownSection.DeprecationNotice d -> "> **Deprecated:** " + d.message() + "\n";
            case MarkdownSection.RawText r -> r.text() + "\n";
        };
    }

    private String renderToc(MarkdownSection.TableOfContents toc) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Table of Contents\n\n");
        for (var entry : toc.entries()) {
            sb.append("- [").append(entry.displayName()).append("](#").append(entry.anchor()).append(")\n");
        }
        return sb.toString();
    }

    private String renderGroupHeader(MarkdownSection.GroupHeader g) {
        StringBuilder sb = new StringBuilder();
        sb.append("## ").append(g.displayName()).append("\n");
        if (g.description() != null && !g.description().isEmpty()) {
            sb.append("\n").append(g.description()).append("\n");
        }
        return sb.toString();
    }

    private String renderPropertyTable(MarkdownSection.PropertyTable table) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        sb.append("| Property | Type | Description | Default | Required |");
        if (table.includeValidation()) {
            sb.append(" Constraints |");
        }
        if (table.includeExamples()) {
            sb.append(" Examples |");
        }
        sb.append("\n");

        sb.append("|----------|------|-------------|---------|----------|");
        if (table.includeValidation()) {
            sb.append("-------------|");
        }
        if (table.includeExamples()) {
            sb.append("----------|");
        }
        sb.append("\n");

        for (PropertyMetadata prop : table.properties()) {
            sb.append("| `").append(prop.name()).append("` ");
            sb.append("| `").append(typeDisplayOf(prop)).append("` ");
            sb.append("| ").append(escapeCell(prop.description())).append(" ");
            sb.append("| ").append(defaultValueOf(prop)).append(" ");
            sb.append("| ").append(prop.required() ? "Yes" : "No").append(" ");
            sb.append("|");

            if (table.includeValidation()) {
                sb.append(" ").append(formatConstraints(prop.constraints())).append(" |");
            }
            if (table.includeExamples()) {
                sb.append(" ").append(formatExamples(prop.examples())).append(" |");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String typeDisplayOf(PropertyMetadata prop) {
        if (prop.typeDisplay() != null && !prop.typeDisplay().isEmpty()) {
            return prop.typeDisplay();
        }
        if (prop.type() == null) return "";
        int dot = prop.type().lastIndexOf('.');
        return dot >= 0 ? prop.type().substring(dot + 1) : prop.type();
    }

    private String defaultValueOf(PropertyMetadata prop) {
        if (prop.defaultValue() == null || prop.defaultValue().isEmpty()) {
            return "";
        }
        return "`" + prop.defaultValue() + "`";
    }

    public String escapeCell(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.replace("|", "\\|").replace("\n", " ");
    }

    public String tableHeader(String... headers) {
        StringBuilder sb = new StringBuilder("|");
        StringBuilder separator = new StringBuilder("|");
        for (String h : headers) {
            sb.append(" ").append(h).append(" |");
            separator.append("-".repeat(h.length() + 2)).append("|");
        }
        return sb + "\n" + separator + "\n";
    }

    public String tableRow(String... cells) {
        StringBuilder sb = new StringBuilder("|");
        for (String cell : cells) {
            sb.append(" ").append(escapeCell(cell)).append(" |");
        }
        return sb + "\n";
    }

    private String formatConstraints(List<ValidationConstraint> constraints) {
        if (constraints == null || constraints.isEmpty()) return "";
        return constraints.stream()
                .map(c -> c.type() + "(" + c.description() + ")")
                .collect(Collectors.joining(", "));
    }

    private String formatExamples(List<ExampleValue> examples) {
        if (examples == null || examples.isEmpty()) return "";
        return examples.stream()
                .map(e -> {
                    String s = "`" + e.value() + "`";
                    if (e.description() != null && !e.description().isEmpty()) {
                        s += " (" + e.description() + ")";
                    }
                    return s;
                })
                .collect(Collectors.joining(", "));
    }

    static String toAnchor(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
    }
}
