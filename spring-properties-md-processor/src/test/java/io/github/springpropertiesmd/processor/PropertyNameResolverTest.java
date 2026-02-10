package io.github.springpropertiesmd.processor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyNameResolverTest {

    private final PropertyNameResolver resolver = new PropertyNameResolver();

    @Test
    void simpleFieldWithPrefix() {
        assertThat(resolver.resolve("app.server", "port")).isEqualTo("app.server.port");
    }

    @Test
    void camelCaseToKebabCase() {
        assertThat(resolver.resolve("app", "serverPort")).isEqualTo("app.server-port");
    }

    @Test
    void emptyPrefix() {
        assertThat(resolver.resolve("", "port")).isEqualTo("port");
    }

    @Test
    void nullPrefix() {
        assertThat(resolver.resolve(null, "port")).isEqualTo("port");
    }

    @Test
    void alreadyKebabCase() {
        assertThat(resolver.resolve("app", "port")).isEqualTo("app.port");
    }

    @Test
    void multipleUpperCaseLetters() {
        assertThat(resolver.resolve("app", "maxHTTPConnections"))
                .isEqualTo("app.max-h-t-t-p-connections");
    }
}
