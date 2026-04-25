package io.github.springpropertiesmd.core.generator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RenderedDocumentationTest {

    @Test
    void singleContentReturnsOnlyRenderedFile() {
        var rendered = new RenderedDocumentation(Map.of(Path.of("docs/configuration.md"), "# Docs\n"));

        assertThat(rendered.singleContent()).isEqualTo("# Docs\n");
    }

    @Test
    void normalizesLineEndingsAndTrailingWhitespaceForDiffs() {
        String normalized = RenderedDocumentation.normalize("# Docs \r\n\n");

        assertThat(normalized).isEqualTo("# Docs\n");
    }
}
