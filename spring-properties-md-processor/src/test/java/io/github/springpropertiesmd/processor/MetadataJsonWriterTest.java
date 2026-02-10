package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataJsonWriterTest {

    private final MetadataJsonWriter writer = new MetadataJsonWriter();

    @Test
    void serializeAndDeserializeEmptyBundle() throws IOException {
        DocumentationBundle bundle = DocumentationBundle.empty();
        String json = writer.toJson(bundle);
        DocumentationBundle result = writer.fromJson(json);

        assertThat(result.groups()).isEmpty();
        assertThat(result.properties()).isEmpty();
    }

    @Test
    void serializeAndDeserializeBundleWithData() throws IOException {
        var group = new GroupMetadata("app.server", "Server Config", "HTTP server settings",
                "com.example.ServerProperties", "Server", 1);
        var property = new PropertyMetadata(
                "app.server.port", "java.lang.Integer", "Integer",
                "Server port", "8080", true, false,
                List.of("dev"), null,
                List.of(new ExampleValue("8080", "default")),
                List.of(new ValidationConstraint("Min", "must be at least 1")),
                "Server", "", "1.0",
                List.of("app.server.host"),
                Map.of("env", "PORT"),
                "com.example.ServerProperties", "app.server"
        );
        var bundle = new DocumentationBundle(List.of(group), List.of(property));

        String json = writer.toJson(bundle);
        DocumentationBundle result = writer.fromJson(json);

        assertThat(result.groups()).hasSize(1);
        assertThat(result.groups().getFirst().name()).isEqualTo("app.server");
        assertThat(result.groups().getFirst().displayName()).isEqualTo("Server Config");

        assertThat(result.properties()).hasSize(1);
        assertThat(result.properties().getFirst().name()).isEqualTo("app.server.port");
        assertThat(result.properties().getFirst().required()).isTrue();
        assertThat(result.properties().getFirst().examples()).hasSize(1);
        assertThat(result.properties().getFirst().constraints()).hasSize(1);
    }

    @Test
    void serializeWithDeprecation() throws IOException {
        var deprecation = new DeprecationInfo("outdated", "app.server.new-port", "1.0", "2.0");
        var property = new PropertyMetadata(
                "app.server.old-port", "Integer", null,
                "Old port", null, false, false,
                null, deprecation, null, null,
                null, null, null, null, null, null, null
        );
        var bundle = new DocumentationBundle(List.of(), List.of(property));

        String json = writer.toJson(bundle);
        DocumentationBundle result = writer.fromJson(json);

        assertThat(result.properties().getFirst().deprecation()).isNotNull();
        assertThat(result.properties().getFirst().deprecation().reason()).isEqualTo("outdated");
        assertThat(result.properties().getFirst().deprecation().replacedBy()).isEqualTo("app.server.new-port");
    }

    @Test
    void jsonContainsExpectedFields() throws IOException {
        var bundle = new DocumentationBundle(
                List.of(new GroupMetadata("test", "Test", "desc", "com.Test", "cat", 0)),
                List.of()
        );
        String json = writer.toJson(bundle);

        assertThat(json).contains("\"groups\"");
        assertThat(json).contains("\"properties\"");
        assertThat(json).contains("\"name\"");
        assertThat(json).contains("\"displayName\"");
    }
}
