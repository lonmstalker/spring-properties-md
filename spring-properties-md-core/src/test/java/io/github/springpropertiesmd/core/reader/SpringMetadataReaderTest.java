package io.github.springpropertiesmd.core.reader;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SpringMetadataReaderTest {

    private final SpringMetadataReader reader = new SpringMetadataReader();

    @Test
    void readSpringMetadataJson(@TempDir Path tempDir) throws IOException {
        String json = """
                {
                  "groups": [
                    {"name": "server", "sourceType": "com.example.ServerProperties"}
                  ],
                  "properties": [
                    {"name": "server.port", "type": "java.lang.Integer", "description": "Server port", "sourceType": "com.example.ServerProperties", "defaultValue": 8080}
                  ]
                }
                """;
        Path file = tempDir.resolve("spring-configuration-metadata.json");
        Files.writeString(file, json);

        DocumentationBundle bundle = reader.read(file);

        assertThat(bundle.groups()).hasSize(1);
        assertThat(bundle.groups().getFirst().name()).isEqualTo("server");
        assertThat(bundle.properties()).hasSize(1);
        assertThat(bundle.properties().getFirst().name()).isEqualTo("server.port");
        assertThat(bundle.properties().getFirst().description()).isEqualTo("Server port");
    }

    @Test
    void readWithDeprecation() throws IOException {
        String json = """
                {
                  "groups": [],
                  "properties": [
                    {"name": "old.prop", "type": "String", "deprecation": {"reason": "outdated", "replacement": "new.prop"}}
                  ]
                }
                """;
        var is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        DocumentationBundle bundle = reader.read(is);

        assertThat(bundle.properties()).hasSize(1);
        assertThat(bundle.properties().getFirst().deprecation()).isNotNull();
        assertThat(bundle.properties().getFirst().deprecation().reason()).isEqualTo("outdated");
        assertThat(bundle.properties().getFirst().deprecation().replacedBy()).isEqualTo("new.prop");
    }

    @Test
    void readEmptyMetadata() throws IOException {
        String json = """
                {"groups": [], "properties": []}
                """;
        var is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        DocumentationBundle bundle = reader.read(is);
        assertThat(bundle.groups()).isEmpty();
        assertThat(bundle.properties()).isEmpty();
    }

    @Test
    void readWithMissingFields() throws IOException {
        String json = """
                {
                  "properties": [
                    {"name": "app.name"}
                  ]
                }
                """;
        var is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        DocumentationBundle bundle = reader.read(is);
        assertThat(bundle.properties()).hasSize(1);
        assertThat(bundle.properties().getFirst().name()).isEqualTo("app.name");
        assertThat(bundle.properties().getFirst().type()).isNull();
    }
}
