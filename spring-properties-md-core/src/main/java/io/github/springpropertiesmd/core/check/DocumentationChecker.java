package io.github.springpropertiesmd.core.check;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.PropertyMetadata;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.config.OutputStyle;
import io.github.springpropertiesmd.core.generator.MarkdownGenerator;
import io.github.springpropertiesmd.core.generator.RenderedDocumentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentationChecker {

    private final MarkdownGenerator generator;

    public DocumentationChecker(MarkdownGenerator generator) {
        this.generator = generator;
    }

    public DocumentationCheckResult check(DocumentationBundle bundle, GeneratorConfig generatorConfig,
                                          CheckConfig checkConfig) {
        List<DocumentationIssue> issues = new ArrayList<>();
        CheckConfig config = checkConfig == null ? CheckConfig.defaults() : checkConfig;

        for (PropertyMetadata property : bundle.properties()) {
            checkProperty(property, config, issues);
        }
        checkDuplicates(bundle, config, issues);
        if (config.failIfGeneratedDocsChanged()) {
            checkGeneratedDocs(bundle, generatorConfig, issues);
        }

        return new DocumentationCheckResult(issues);
    }

    private void checkProperty(PropertyMetadata property, CheckConfig config, List<DocumentationIssue> issues) {
        if (config.failOnMissingDescription() && isBlank(property.description())) {
            issues.add(DocumentationIssue.property("missing-description", property.name(),
                    "Missing description for property `" + property.name() + "`."));
        }
        if (property.sensitive() && config.failOnSensitiveDefault() && !isBlank(property.defaultValue())) {
            issues.add(DocumentationIssue.property("sensitive-default", property.name(),
                    "Sensitive property `" + property.name() + "` must not expose a default value."));
        }
        if (property.sensitive() && config.failOnSensitiveDefault()
                && property.examples().stream().anyMatch(example -> !isBlank(example.value()))) {
            issues.add(DocumentationIssue.property("sensitive-example", property.name(),
                    "Sensitive property `" + property.name() + "` must not expose example values."));
        }
        if (config.failOnDeprecatedWithoutReplacement() && property.deprecation() != null
                && isBlank(property.deprecation().replacedBy())) {
            issues.add(DocumentationIssue.property("deprecated-without-replacement", property.name(),
                    "Deprecated property `" + property.name() + "` must declare a replacement."));
        }
        if (config.failOnRequiredWithoutExample() && property.required() && !property.sensitive()
                && property.examples().isEmpty()) {
            issues.add(DocumentationIssue.property("required-without-example", property.name(),
                    "Required property `" + property.name() + "` must have at least one example."));
        }
    }

    private void checkDuplicates(DocumentationBundle bundle, CheckConfig config, List<DocumentationIssue> issues) {
        if (!config.failOnDuplicatePropertyNames()) {
            return;
        }
        Map<String, Integer> counts = new HashMap<>();
        for (PropertyMetadata property : bundle.properties()) {
            counts.merge(property.name(), 1, Integer::sum);
        }
        for (var entry : counts.entrySet()) {
            if (entry.getValue() > 1) {
                issues.add(DocumentationIssue.property("duplicate-property-name", entry.getKey(),
                        "Duplicate property name `" + entry.getKey() + "`."));
            }
        }
    }

    private void checkGeneratedDocs(DocumentationBundle bundle, GeneratorConfig config,
                                    List<DocumentationIssue> issues) {
        RenderedDocumentation rendered = generator.render(bundle, config);
        for (var entry : rendered.files().entrySet()) {
            Path path = entry.getKey();
            if (!Files.exists(path)) {
                issues.add(DocumentationIssue.file("generated-docs-missing", path,
                        "Generated documentation file is missing: " + path));
                continue;
            }
            try {
                String existing = Files.readString(path);
                if (!RenderedDocumentation.normalize(existing).equals(RenderedDocumentation.normalize(entry.getValue()))) {
                    issues.add(DocumentationIssue.file("generated-docs-changed", path,
                            "Generated documentation file is out of date: " + path));
                }
            } catch (IOException e) {
                issues.add(DocumentationIssue.file("generated-docs-unreadable", path,
                        "Generated documentation file cannot be read: " + path));
            }
        }

        if (config.outputStyle() != OutputStyle.SINGLE_FILE) {
            checkExtraGeneratedFiles(config.outputDirectory(), rendered.files().keySet(), issues);
        }
    }

    private void checkExtraGeneratedFiles(Path outputDirectory, Set<Path> expectedFiles,
                                          List<DocumentationIssue> issues) {
        if (!Files.isDirectory(outputDirectory)) {
            return;
        }
        Set<Path> expected = new HashSet<>(expectedFiles);
        try (var stream = Files.list(outputDirectory)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".md"))
                    .filter(path -> !expected.contains(path))
                    .forEach(path -> issues.add(DocumentationIssue.file("generated-docs-extra", path,
                            "Generated documentation file is no longer expected: " + path)));
        } catch (IOException e) {
            issues.add(DocumentationIssue.file("generated-docs-unreadable", outputDirectory,
                    "Generated documentation directory cannot be read: " + outputDirectory));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
