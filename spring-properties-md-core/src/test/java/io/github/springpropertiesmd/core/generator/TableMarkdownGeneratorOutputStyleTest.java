package io.github.springpropertiesmd.core.generator;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.config.SensitiveMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableMarkdownGeneratorOutputStyleTest {

    private final TableMarkdownGenerator generator = new TableMarkdownGenerator();

    @Test
    void singleFileRendersConfiguredOutputFile() {
        RenderedDocumentation rendered = generator.render(bundle(), config(OutputStyle.SINGLE_FILE));

        assertThat(rendered.files()).containsOnlyKeys(Path.of("docs/configuration.md"));
        assertThat(rendered.singleContent()).contains("app.server.port", "app.database.url");
    }

    @Test
    void perGroupRendersOneFilePerGroupWithStableNames() {
        RenderedDocumentation rendered = generator.render(bundle(), config(OutputStyle.PER_GROUP));

        assertThat(rendered.files()).containsOnlyKeys(
                Path.of("docs/configuration/app-server.md"),
                Path.of("docs/configuration/app-database.md")
        );
    }

    @Test
    void perCategoryRendersOneFilePerCategoryWithStableNames() {
        RenderedDocumentation rendered = generator.render(bundle(), config(OutputStyle.PER_CATEGORY));

        assertThat(rendered.files()).containsOnlyKeys(
                Path.of("docs/configuration/runtime.md"),
                Path.of("docs/configuration/storage.md")
        );
    }

    private GeneratorConfig config(OutputStyle outputStyle) {
        return new GeneratorConfig(
                Path.of("docs/configuration.md"),
                Path.of("docs/configuration"),
                "Config",
                outputStyle,
                false, true, true, true, SensitiveMode.REDACT, false
        );
    }

    private DocumentationBundle bundle() {
        var server = new GroupMetadata("app.server", "Server Configuration", "",
                "com.example.ServerProperties", "Runtime", 1);
        var database = new GroupMetadata("app.database", "Database Configuration", "",
                "com.example.DatabaseProperties", "Storage", 2);
        return new DocumentationBundle(List.of(server, database), List.of(
                new PropertyMetadata("app.server.port", "java.lang.Integer", null, "Port", null,
                        false, false, null, null, null, null, null, null, null, null, null, null, "app.server"),
                new PropertyMetadata("app.database.url", "java.lang.String", null, "URL", null,
                        false, false, null, null, null, null, null, null, null, null, null, null, "app.database")
        ));
    }
}
