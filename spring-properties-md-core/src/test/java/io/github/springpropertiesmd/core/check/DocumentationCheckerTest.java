package io.github.springpropertiesmd.core.check;

import io.github.springpropertiesmd.api.model.DeprecationInfo;
import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.ExampleValue;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentationCheckerTest {

    private final DocumentationChecker checker = new DocumentationChecker(new TableMarkdownGenerator());

    @Test
    void reportsDefaultQualityGateIssues() {
        DocumentationCheckResult result = checker.check(problemBundle(), GeneratorConfig.defaults(Path.of("docs.md")),
                CheckConfig.defaults());

        assertThat(result.passed()).isFalse();
        assertThat(result.issues()).extracting(DocumentationIssue::rule)
                .contains(
                        "missing-description",
                        "sensitive-default",
                        "sensitive-example",
                        "deprecated-without-replacement",
                        "required-without-example",
                        "duplicate-property-name"
                );
    }

    @Test
    void passesWhenAllDefaultRulesAreSatisfied() {
        var property = new PropertyMetadata("app.name", "java.lang.String", null, "Name", null,
                true, false, null, null, List.of(new ExampleValue("demo", "")), null,
                null, null, null, null, null, null, "app");

        DocumentationCheckResult result = checker.check(new DocumentationBundle(List.of(), List.of(property)),
                GeneratorConfig.defaults(Path.of("docs.md")), CheckConfig.defaults());

        assertThat(result.passed()).isTrue();
    }

    @Test
    void doesNotRequireExamplesForSensitiveRequiredProperties() {
        var property = new PropertyMetadata("app.secret", "java.lang.String", null, "Secret", null,
                true, true, null, null, null, null, null, null, null, null, null, null, "app");

        DocumentationCheckResult result = checker.check(new DocumentationBundle(List.of(), List.of(property)),
                GeneratorConfig.defaults(Path.of("docs.md")), CheckConfig.defaults());

        assertThat(result.passed()).isTrue();
    }

    @Test
    void reportsChangedGeneratedDocsWhenDiffCheckEnabled(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("configuration.md");
        Files.writeString(output, "# Old\n");

        DocumentationCheckResult result = checker.check(validBundle(), GeneratorConfig.defaults(output),
                CheckConfig.defaults().withFailIfGeneratedDocsChanged(true));

        assertThat(result.passed()).isFalse();
        assertThat(result.issues()).extracting(DocumentationIssue::rule).contains("generated-docs-changed");
    }

    private DocumentationBundle validBundle() {
        var property = new PropertyMetadata("app.name", "java.lang.String", null, "Name", null,
                false, false, null, null, null, null, null, null, null, null, null, null, "app");
        return new DocumentationBundle(List.of(), List.of(property));
    }

    private DocumentationBundle problemBundle() {
        var missingDescription = new PropertyMetadata("app.empty", "java.lang.String", null, "", null,
                false, false, null, null, null, null, null, null, null, null, null, null, "app");
        var sensitive = new PropertyMetadata("app.secret", "java.lang.String", null, "Secret", "plain",
                false, true, null, null, List.of(new ExampleValue("plain-example", "")), null,
                null, null, null, null, null, null, "app");
        var deprecated = new PropertyMetadata("app.old", "java.lang.String", null, "Old", null,
                false, false, null, new DeprecationInfo("old", "", "1.0", ""), null, null,
                null, null, null, null, null, null, "app");
        var required = new PropertyMetadata("app.required", "java.lang.String", null, "Required", null,
                true, false, null, null, null, null, null, null, null, null, null, null, "app");
        var duplicateA = new PropertyMetadata("app.dup", "java.lang.String", null, "Duplicate A", null,
                false, false, null, null, null, null, null, null, null, null, null, null, "app");
        var duplicateB = new PropertyMetadata("app.dup", "java.lang.String", null, "Duplicate B", null,
                false, false, null, null, null, null, null, null, null, null, null, null, "app");
        return new DocumentationBundle(List.of(), List.of(missingDescription, sensitive, deprecated,
                required, duplicateA, duplicateB));
    }
}
