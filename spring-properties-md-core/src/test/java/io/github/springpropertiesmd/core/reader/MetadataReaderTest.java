package io.github.springpropertiesmd.core.reader;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetadataReaderTest {

    private final MetadataReader reader = new MetadataReader();

    @Test
    void readValidJson(@TempDir Path tempDir) throws IOException {
        String json = """
                {
                  "groups": [{"name": "app", "displayName": "App", "description": "desc", "sourceType": "com.App", "category": "", "order": 0}],
                  "properties": [{"name": "app.name", "type": "java.lang.String", "description": "Name", "required": false, "sensitive": false}]
                }
                """;
        Path file = tempDir.resolve("metadata.json");
        Files.writeString(file, json);

        DocumentationBundle bundle = reader.read(file);

        assertThat(bundle.groups()).hasSize(1);
        assertThat(bundle.groups().getFirst().name()).isEqualTo("app");
        assertThat(bundle.properties()).hasSize(1);
        assertThat(bundle.properties().getFirst().name()).isEqualTo("app.name");
    }

    @Test
    void readFromInputStream() throws IOException {
        String json = """
                {
                  "groups": [],
                  "properties": [{"name": "test", "type": "String", "required": false, "sensitive": false}]
                }
                """;
        var is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        DocumentationBundle bundle = reader.read(is);
        assertThat(bundle.properties()).hasSize(1);
    }

    @Test
    void readFromClassesDir(@TempDir Path tempDir) throws IOException {
        Path metaDir = tempDir.resolve("META-INF/spring-properties-md");
        Files.createDirectories(metaDir);
        Files.writeString(metaDir.resolve("enriched-metadata.json"),
                """
                {"groups": [], "properties": []}
                """);

        DocumentationBundle bundle = reader.readFromClassesDir(tempDir);
        assertThat(bundle.groups()).isEmpty();
        assertThat(bundle.properties()).isEmpty();
    }

    @Test
    void readFromMissingClassesDirReturnsEmpty(@TempDir Path tempDir) throws IOException {
        DocumentationBundle bundle = reader.readFromClassesDir(tempDir);
        assertThat(bundle.groups()).isEmpty();
        assertThat(bundle.properties()).isEmpty();
    }

    @Test
    void malformedJsonThrowsException(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bad.json");
        Files.writeString(file, "not valid json");

        assertThatThrownBy(() -> reader.read(file)).isInstanceOf(IOException.class);
    }

    @Test
    void mergeBundles() {
        var b1 = new DocumentationBundle(
                List.of(new GroupMetadata("g1", "G1", "", "", "", 0)),
                List.of()
        );
        var b2 = new DocumentationBundle(
                List.of(new GroupMetadata("g2", "G2", "", "", "", 1)),
                List.of(new PropertyMetadata("p1", "String", null, "desc", null,
                        false, false, null, null, null, null, null, null, null, null, null, null, null))
        );

        DocumentationBundle merged = reader.merge(List.of(b1, b2));
        assertThat(merged.groups()).hasSize(2);
        assertThat(merged.properties()).hasSize(1);
    }
}
