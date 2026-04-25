package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.*;
import io.github.springpropertiesmd.core.config.ConditionConfig;
import io.github.springpropertiesmd.core.config.ExternalConditionMode;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TableMarkdownGeneratorTest {

    private final TableMarkdownGenerator generator = new TableMarkdownGenerator();

    private GeneratorConfig defaultConfig() {
        return GeneratorConfig.defaults(Path.of("output.md"));
    }

    @Test
    void emptyBundleProducesTitle() {
        String result = generator.generate(DocumentationBundle.empty(), defaultConfig());
        assertThat(result).contains("# Configuration Properties");
    }

    @Test
    void singleGroupWithProperties() {
        var group = new GroupMetadata("app.server", "Server Configuration",
                "HTTP server settings", "com.example.ServerProperties", "Server", 1);
        var property = new PropertyMetadata(
                "app.server.port", "java.lang.Integer", "Integer",
                "Server port", "8080", true, false,
                null, null,
                List.of(new ExampleValue("8080", "dev")),
                List.of(new ValidationConstraint("Min", "must be at least 1")),
                null, null, null, null, null, null, "app.server"
        );
        var bundle = new DocumentationBundle(List.of(group), List.of(property));
        String result = generator.generate(bundle, defaultConfig());

        assertThat(result).contains("# Configuration Properties");
        assertThat(result).contains("## Table of Contents");
        assertThat(result).contains("## Server Configuration");
        assertThat(result).contains("HTTP server settings");
        assertThat(result).contains("| `app.server.port`");
        assertThat(result).contains("| Yes ");
    }

    @Test
    void multipleGroupsSortedByOrder() {
        var group1 = new GroupMetadata("app.db", "Database", "DB settings",
                "com.example.DbProperties", "Database", 2);
        var group2 = new GroupMetadata("app.server", "Server", "HTTP settings",
                "com.example.ServerProperties", "Server", 1);
        var prop1 = simpleProperty("app.db.url", "DB URL", "app.db");
        var prop2 = simpleProperty("app.server.port", "Port", "app.server");
        var bundle = new DocumentationBundle(List.of(group1, group2), List.of(prop1, prop2));

        String result = generator.generate(bundle, defaultConfig());

        int serverIdx = result.indexOf("## Server");
        int dbIdx = result.indexOf("## Database");
        assertThat(serverIdx).isLessThan(dbIdx);
    }

    @Test
    void excludeDeprecatedWhenConfigured() {
        var group = new GroupMetadata("app", "App", "", "com.App", "", 0);
        var deprecated = new PropertyMetadata(
                "app.old", "String", null, "Old prop", null,
                false, false, null,
                new DeprecationInfo("old", "app.new", "1.0", "2.0"),
                null, null, null, null, null, null, null, null, "app"
        );
        var bundle = new DocumentationBundle(List.of(group), List.of(deprecated));

        var config = new GeneratorConfig(
                Path.of("output.md"), Path.of("output"), "Config", OutputStyle.SINGLE_FILE,
                false, false, true, true, SensitiveMode.REDACT, false
        );

        String result = generator.generate(bundle, config);
        assertThat(result).doesNotContain("app.old");
    }

    @Test
    void includeDeprecatedByDefault() {
        var group = new GroupMetadata("app", "App", "", "com.App", "", 0);
        var deprecated = new PropertyMetadata(
                "app.old", "String", null, "Old prop", null,
                false, false, null,
                new DeprecationInfo("old", "app.new", "1.0", "2.0"),
                null, null, null, null, null, null, null, null, "app"
        );
        var bundle = new DocumentationBundle(List.of(group), List.of(deprecated));

        String result = generator.generate(bundle, defaultConfig());
        assertThat(result).contains("app.old");
    }

    @Test
    void sensitivePropertyDisplayed() {
        var group = new GroupMetadata("app", "App", "", "com.App", "", 0);
        var prop = new PropertyMetadata(
                "app.secret", "String", null, "Secret key", null,
                false, true, null, null, null, null,
                null, null, null, null, null, null, "app"
        );
        var bundle = new DocumentationBundle(List.of(group), List.of(prop));

        String result = generator.generate(bundle, defaultConfig());
        assertThat(result).contains("app.secret");
    }

    @Test
    void noTableOfContentsWhenDisabled() {
        var group = new GroupMetadata("app", "App", "", "com.App", "", 0);
        var prop = simpleProperty("app.name", "Name", "app");
        var bundle = new DocumentationBundle(List.of(group), List.of(prop));

        var config = new GeneratorConfig(
                Path.of("output.md"), Path.of("output"), "Config", OutputStyle.SINGLE_FILE,
                false, true, true, true, SensitiveMode.REDACT, false
        );

        String result = generator.generate(bundle, config);
        assertThat(result).doesNotContain("Table of Contents");
    }

    @Test
    void customTitle() {
        var config = new GeneratorConfig(
                Path.of("output.md"), Path.of("output"), "My Custom Title", OutputStyle.SINGLE_FILE,
                false, true, true, true, SensitiveMode.REDACT, false
        );
        String result = generator.generate(DocumentationBundle.empty(), config);
        assertThat(result).contains("# My Custom Title");
    }

    @Test
    void rendersLocalGroupConditionsInMainDocs() {
        var group = new GroupMetadata("app.redis", "Redis", "", "com.Redis", "", 0);
        var enabled = simpleProperty("app.redis.enabled", "Enabled", "app.redis");
        var host = simpleProperty("app.redis.host", "Host", "app.redis");
        var condition = condition("com.RedisConfiguration", "app.redis",
                requirement("app.redis.enabled", "true", true, true));
        var bundle = new DocumentationBundle(List.of(group), List.of(enabled, host), List.of(condition));

        String result = generator.generate(bundle, defaultConfig());

        assertThat(result).contains("Applies when:");
        assertThat(result).contains("- `app.redis.enabled=true`, or the property is missing");
        assertThat(result).contains("Effective when");
        assertThat(result).contains("| `app.redis.host` | `String` | Host |  | No |  |  | `app.redis.enabled=true`, or the property is missing |");
    }

    @Test
    void externalConditionsAreNotRenderedInMainDocs() {
        var group = new GroupMetadata("app.redis", "Redis", "", "com.Redis", "", 0);
        var host = simpleProperty("app.redis.host", "Host", "app.redis");
        var condition = condition("com.RedisConfiguration", "app.redis",
                requirement("spring.datasource.url", "", false, false));
        var bundle = new DocumentationBundle(List.of(group), List.of(host), List.of(condition));

        String result = generator.generate(bundle, defaultConfig());

        assertThat(result).doesNotContain("Effective when");
        assertThat(result).doesNotContain("spring.datasource.url");
    }

    @Test
    void rendersSeparateExternalConditionsFile() {
        var group = new GroupMetadata("app.redis", "Redis", "", "com.Redis", "", 0);
        var host = simpleProperty("app.redis.host", "Host", "app.redis");
        var condition = condition("com.RedisConfiguration", "app.redis",
                requirement("spring.datasource.url", "", false, false));
        var bundle = new DocumentationBundle(List.of(group), List.of(host), List.of(condition));
        var config = new GeneratorConfig(
                Path.of("output.md"), Path.of("output"), "Config", OutputStyle.SINGLE_FILE,
                false, true, true, true, SensitiveMode.REDACT, false,
                new ConditionConfig(true, true, ExternalConditionMode.SEPARATE_FILE, Path.of("external.md"))
        );

        RenderedDocumentation rendered = generator.render(bundle, config);

        assertThat(rendered.files()).containsKey(Path.of("output.md"));
        assertThat(rendered.files()).containsKey(Path.of("external.md"));
        assertThat(rendered.files().get(Path.of("external.md")))
                .contains("# External Property Conditions")
                .contains("## `spring.datasource.url`")
                .contains("`spring.datasource.url` must be present and not equal to `false`");
    }

    private PropertyMetadata simpleProperty(String name, String description, String groupName) {
        return new PropertyMetadata(name, "java.lang.String", null, description, null,
                false, false, null, null, null, null, null, null, null, null, null, null, groupName);
    }

    private PropertyConditionMetadata condition(String sourceElement, String ownerId, PropertyRequirement... requirements) {
        return new PropertyConditionMetadata(sourceElement, ownerId, ConditionOwnerType.PROPERTY_GROUP, List.of(requirements));
    }

    private PropertyRequirement requirement(String propertyName, String havingValue, boolean matchIfMissing,
                                            boolean local) {
        PropertyConditionMatchMode mode = havingValue == null || havingValue.isBlank()
                ? PropertyConditionMatchMode.PRESENT_AND_NOT_FALSE
                : PropertyConditionMatchMode.EQUALS_VALUE;
        return new PropertyRequirement(propertyName, havingValue, matchIfMissing, mode, local);
    }
}
