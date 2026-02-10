package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.ExampleValue;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.api.model.ValidationConstraint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownFormatterTest {

    private final MarkdownFormatter formatter = new MarkdownFormatter();

    @Test
    void renderTitle() {
        String result = formatter.render(new MarkdownSection.Title("My Title"));
        assertThat(result).isEqualTo("# My Title\n");
    }

    @Test
    void renderTableOfContents() {
        var toc = new MarkdownSection.TableOfContents(List.of(
                new MarkdownSection.TableOfContents.TocEntry("Server Config", "server-config"),
                new MarkdownSection.TableOfContents.TocEntry("Database", "database")
        ));
        String result = formatter.render(toc);
        assertThat(result).contains("## Table of Contents");
        assertThat(result).contains("- [Server Config](#server-config)");
        assertThat(result).contains("- [Database](#database)");
    }

    @Test
    void renderGroupHeader() {
        String result = formatter.render(new MarkdownSection.GroupHeader("Server Config", "HTTP server settings"));
        assertThat(result).contains("## Server Config");
        assertThat(result).contains("HTTP server settings");
    }

    @Test
    void renderGroupHeaderWithoutDescription() {
        String result = formatter.render(new MarkdownSection.GroupHeader("Server Config", ""));
        assertThat(result).contains("## Server Config");
        assertThat(result).doesNotContain("\n\n");
    }

    @Test
    void renderPropertyTable() {
        var prop = new PropertyMetadata(
                "app.server.port", "java.lang.Integer", "Integer",
                "Server port", "8080", true, false,
                null, null,
                List.of(new ExampleValue("8080", "dev")),
                List.of(new ValidationConstraint("Min", "must be at least 1")),
                null, null, null, null, null, null, null
        );
        var table = new MarkdownSection.PropertyTable(List.of(prop), true, true);
        String result = formatter.render(table);

        assertThat(result).contains("| Property |");
        assertThat(result).contains("| `app.server.port` ");
        assertThat(result).contains("| `Integer` ");
        assertThat(result).contains("| Server port ");
        assertThat(result).contains("| `8080` ");
        assertThat(result).contains("| Yes ");
        assertThat(result).contains("Min(must be at least 1)");
        assertThat(result).contains("`8080` (dev)");
    }

    @Test
    void renderDeprecationNotice() {
        String result = formatter.render(new MarkdownSection.DeprecationNotice("Use new property"));
        assertThat(result).contains("**Deprecated:**");
        assertThat(result).contains("Use new property");
    }

    @Test
    void renderRawText() {
        String result = formatter.render(new MarkdownSection.RawText("Some raw text"));
        assertThat(result).isEqualTo("Some raw text\n");
    }

    @Test
    void escapeCellPipeCharacter() {
        assertThat(formatter.escapeCell("a|b")).isEqualTo("a\\|b");
    }

    @Test
    void escapeCellNewline() {
        assertThat(formatter.escapeCell("line1\nline2")).isEqualTo("line1 line2");
    }

    @Test
    void escapeCellNull() {
        assertThat(formatter.escapeCell(null)).isEmpty();
    }

    @Test
    void tableHeader() {
        String result = formatter.tableHeader("Name", "Type");
        assertThat(result).contains("| Name | Type |");
        assertThat(result).contains("|------|------|");
    }

    @Test
    void tableRow() {
        String result = formatter.tableRow("a", "b");
        assertThat(result).isEqualTo("| a | b |\n");
    }

    @Test
    void toAnchor() {
        assertThat(MarkdownFormatter.toAnchor("Server Configuration")).isEqualTo("server-configuration");
        assertThat(MarkdownFormatter.toAnchor("DB (Main)")).isEqualTo("db-main");
    }
}
