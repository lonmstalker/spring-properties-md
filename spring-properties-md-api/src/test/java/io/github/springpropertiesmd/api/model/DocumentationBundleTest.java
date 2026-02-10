package io.github.springpropertiesmd.api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentationBundleTest {

    @Test
    void emptyBundleHasNoGroupsOrProperties() {
        var bundle = DocumentationBundle.empty();
        assertThat(bundle.groups()).isEmpty();
        assertThat(bundle.properties()).isEmpty();
    }

    @Test
    void defensiveCopyOfGroups() {
        var groups = new ArrayList<>(List.of(
                new GroupMetadata("server", "Server", "desc", "com.example.Server", "Server", 0)
        ));
        var bundle = new DocumentationBundle(groups, List.of());

        groups.add(new GroupMetadata("db", "Database", "desc", "com.example.Db", "Database", 1));
        assertThat(bundle.groups()).hasSize(1);
    }

    @Test
    void defensiveCopyOfProperties() {
        var props = new ArrayList<>(List.of(
                new PropertyMetadata("app.name", "String", null, null, null,
                        false, false, null, null, null, null,
                        null, null, null, null, null, null, null)
        ));
        var bundle = new DocumentationBundle(List.of(), props);

        props.add(new PropertyMetadata("app.port", "Integer", null, null, null,
                false, false, null, null, null, null,
                null, null, null, null, null, null, null));
        assertThat(bundle.properties()).hasSize(1);
    }

    @Test
    void groupsListIsUnmodifiable() {
        var bundle = new DocumentationBundle(
                List.of(new GroupMetadata("server", "Server", "desc", "com.example.Server", "Server", 0)),
                List.of()
        );

        assertThatThrownBy(() -> bundle.groups().add(
                new GroupMetadata("db", "Database", "desc", "com.example.Db", "Database", 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullListsDefaultToEmpty() {
        var bundle = new DocumentationBundle(null, null);
        assertThat(bundle.groups()).isEmpty();
        assertThat(bundle.properties()).isEmpty();
    }
}
