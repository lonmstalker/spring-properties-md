package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpringMetadataMergerTest {

    private final SpringMetadataMerger merger = new SpringMetadataMerger();

    @Test
    void mergeWithEmptySpringMetadata() {
        var enriched = new DocumentationBundle(
                List.of(new GroupMetadata("app", "App", "desc", "com.App", "General", 0)),
                List.of(simpleProperty("app.name", "App name"))
        );

        DocumentationBundle result = merger.merge(enriched, DocumentationBundle.empty());

        assertThat(result.groups()).hasSize(1);
        assertThat(result.properties()).hasSize(1);
    }

    @Test
    void enrichedOverridesSpringMetadata() {
        var spring = new DocumentationBundle(List.of(),
                List.of(simpleProperty("app.name", "Spring description")));
        var enriched = new DocumentationBundle(List.of(),
                List.of(simpleProperty("app.name", "Enriched description")));

        DocumentationBundle result = merger.merge(enriched, spring);

        assertThat(result.properties()).hasSize(1);
        assertThat(result.properties().getFirst().description()).isEqualTo("Enriched description");
    }

    @Test
    void springPropertiesKeptWhenNotInEnriched() {
        var spring = new DocumentationBundle(List.of(),
                List.of(simpleProperty("app.extra", "Spring only")));
        var enriched = new DocumentationBundle(List.of(),
                List.of(simpleProperty("app.name", "Enriched")));

        DocumentationBundle result = merger.merge(enriched, spring);

        assertThat(result.properties()).hasSize(2);
    }

    @Test
    void groupsAreMerged() {
        var spring = new DocumentationBundle(
                List.of(new GroupMetadata("app", "App", "spring desc", "com.App", "", 0)),
                List.of());
        var enriched = new DocumentationBundle(
                List.of(new GroupMetadata("app", "Application", "enriched desc", "com.App", "General", 1)),
                List.of());

        DocumentationBundle result = merger.merge(enriched, spring);

        assertThat(result.groups()).hasSize(1);
        assertThat(result.groups().getFirst().displayName()).isEqualTo("Application");
    }

    @Test
    void mergePropertyFillsGapsFromSpring() {
        var spring = new DocumentationBundle(List.of(),
                List.of(new PropertyMetadata("app.name", "java.lang.String", "String",
                        "Spring desc", "default", false, false,
                        null, null, null, null, null, null, null, null, null, "com.App", "app")));
        var enriched = new DocumentationBundle(List.of(),
                List.of(new PropertyMetadata("app.name", "java.lang.String", null,
                        "Enriched desc", null, true, false,
                        null, null, null, null, null, null, null, null, null, null, null)));

        DocumentationBundle result = merger.merge(enriched, spring);

        PropertyMetadata merged = result.properties().getFirst();
        assertThat(merged.description()).isEqualTo("Enriched desc");
        assertThat(merged.defaultValue()).isEqualTo("default");
        assertThat(merged.required()).isTrue();
        assertThat(merged.typeDisplay()).isEqualTo("String");
        assertThat(merged.sourceType()).isEqualTo("com.App");
    }

    private PropertyMetadata simpleProperty(String name, String description) {
        return new PropertyMetadata(name, "java.lang.String", null, description, null,
                false, false, null, null, null, null, null, null, null, null, null, null, null);
    }
}
