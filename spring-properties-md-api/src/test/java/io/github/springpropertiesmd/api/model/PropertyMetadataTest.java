package io.github.springpropertiesmd.api.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyMetadataTest {

    @Test
    void accessorsReturnCorrectValues() {
        var metadata = new PropertyMetadata(
                "app.server.port", "java.lang.Integer", "Integer",
                "Server port", "8080", true, false,
                List.of("dev", "prod"), null, List.of(), List.of(),
                "Server", "", "1.0", List.of("app.server.host"),
                Map.of("env", "PORT"), "com.example.ServerProperties", "server"
        );

        assertThat(metadata.name()).isEqualTo("app.server.port");
        assertThat(metadata.type()).isEqualTo("java.lang.Integer");
        assertThat(metadata.description()).isEqualTo("Server port");
        assertThat(metadata.defaultValue()).isEqualTo("8080");
        assertThat(metadata.required()).isTrue();
        assertThat(metadata.sensitive()).isFalse();
        assertThat(metadata.profiles()).containsExactly("dev", "prod");
        assertThat(metadata.seeAlso()).containsExactly("app.server.host");
        assertThat(metadata.customMetadata()).containsEntry("env", "PORT");
    }

    @Test
    void nullCollectionsDefaultToEmpty() {
        var metadata = new PropertyMetadata(
                "app.name", "String", null, null, null,
                false, false, null, null, null, null,
                null, null, null, null, null, null, null
        );

        assertThat(metadata.profiles()).isEmpty();
        assertThat(metadata.examples()).isEmpty();
        assertThat(metadata.constraints()).isEmpty();
        assertThat(metadata.seeAlso()).isEmpty();
        assertThat(metadata.customMetadata()).isEmpty();
    }

    @Test
    void defensiveCopyOfProfiles() {
        var profiles = new java.util.ArrayList<>(List.of("dev"));
        var metadata = new PropertyMetadata(
                "app.name", "String", null, null, null,
                false, false, profiles, null, null, null,
                null, null, null, null, null, null, null
        );

        profiles.add("prod");
        assertThat(metadata.profiles()).containsExactly("dev");
    }

    @Test
    void profilesListIsUnmodifiable() {
        var metadata = new PropertyMetadata(
                "app.name", "String", null, null, null,
                false, false, List.of("dev"), null, null, null,
                null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> metadata.profiles().add("prod"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void equalsAndHashCodeWork() {
        var m1 = new PropertyMetadata(
                "app.name", "String", null, "desc", null,
                false, false, List.of(), null, List.of(), List.of(),
                null, null, null, List.of(), Map.of(), null, null
        );
        var m2 = new PropertyMetadata(
                "app.name", "String", null, "desc", null,
                false, false, List.of(), null, List.of(), List.of(),
                null, null, null, List.of(), Map.of(), null, null
        );

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }
}
