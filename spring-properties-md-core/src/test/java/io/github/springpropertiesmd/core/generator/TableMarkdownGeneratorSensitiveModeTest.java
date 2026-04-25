package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.ExampleValue;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableMarkdownGeneratorSensitiveModeTest {

    private final TableMarkdownGenerator generator = new TableMarkdownGenerator();

    @Test
    void showSensitiveRendersDefaultsAndExamples() {
        String markdown = generator.generate(bundle(), config(SensitiveMode.SHOW));

        assertThat(markdown).contains("`secret-default`");
        assertThat(markdown).contains("`secret-example`");
    }

    @Test
    void redactSensitiveMasksDefaultsAndExamples() {
        String markdown = generator.generate(bundle(), config(SensitiveMode.REDACT));

        assertThat(markdown).contains("`***`");
        assertThat(markdown).doesNotContain("secret-default");
        assertThat(markdown).doesNotContain("secret-example");
        assertThat(markdown).contains("app.secret");
    }

    @Test
    void omitSensitiveRemovesProperty() {
        String markdown = generator.generate(bundle(), config(SensitiveMode.OMIT));

        assertThat(markdown).doesNotContain("app.secret");
        assertThat(markdown).contains("app.name");
    }

    private GeneratorConfig config(SensitiveMode mode) {
        return new GeneratorConfig(
                Path.of("configuration.md"),
                Path.of("configuration"),
                "Config",
                OutputStyle.SINGLE_FILE,
                false, true, true, true, mode, false
        );
    }

    private DocumentationBundle bundle() {
        var group = new GroupMetadata("app", "App", "", "com.example.AppProperties", "", 0);
        return new DocumentationBundle(List.of(group), List.of(
                new PropertyMetadata("app.name", "java.lang.String", null, "Name", null,
                        false, false, null, null, null, null, null, null, null, null, null, null, "app"),
                new PropertyMetadata("app.secret", "java.lang.String", null, "Secret", "secret-default",
                        false, true, null, null, List.of(new ExampleValue("secret-example", "secret")), null,
                        null, null, null, null, null, null, "app")
        ));
    }
}
