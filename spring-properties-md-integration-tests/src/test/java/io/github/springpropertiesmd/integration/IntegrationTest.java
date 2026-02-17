package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.core.config.GeneratorConfig;
import io.github.springpropertiesmd.core.generator.TableMarkdownGenerator;
import io.github.springpropertiesmd.core.reader.MetadataReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

    @Test
    void annotationProcessorGeneratesEnrichedMetadata() {
        Path classesDir = Path.of("target/classes");
        Path metadataFile = classesDir.resolve("META-INF/spring-properties-md/enriched-metadata.json");

        assertThat(metadataFile).exists();
    }

    @Test
    void enrichedMetadataContainsAllGroups() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.groups()).isNotEmpty();
        assertThat(bundle.groups()).anyMatch(g -> g.displayName().equals("Server Configuration"));
        assertThat(bundle.groups()).anyMatch(g -> g.displayName().equals("Database Configuration"));
        assertThat(bundle.groups()).anyMatch(g -> g.displayName().equals("Security Configuration"));
        assertThat(bundle.groups()).anyMatch(g -> g.displayName().equals("Collection Configuration"));
    }

    @Test
    void enrichedMetadataContainsServerProperties() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.server.port") && p.required() && p.description().equals("Server port number"));
        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.server.host") && !p.required());
    }

    @Test
    void enrichedMetadataContainsSensitiveProperties() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.database.password") && p.sensitive());
        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.security.jwt-secret") && p.sensitive());
    }

    @Test
    void enrichedMetadataContainsDeprecatedProperties() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.security.api-key") && p.deprecation() != null
                        && p.deprecation().replacedBy().equals("app.security.jwt-secret"));
    }

    @Test
    void enrichedMetadataContainsExamples() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.server.port") && !p.examples().isEmpty()
                        && p.examples().getFirst().value().equals("8080"));
    }

    @Test
    void fullPipelineGeneratesMarkdown(@TempDir Path tempDir) throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        Path outputFile = tempDir.resolve("configuration-properties.md");
        GeneratorConfig config = GeneratorConfig.defaults(outputFile);

        TableMarkdownGenerator generator = new TableMarkdownGenerator();
        String markdown = generator.generate(bundle, config);

        Files.writeString(outputFile, markdown);

        assertThat(outputFile).exists();
        String content = Files.readString(outputFile);

        assertThat(content).contains("# Configuration Properties");
        assertThat(content).contains("## Table of Contents");
        assertThat(content).contains("## Server Configuration");
        assertThat(content).contains("## Database Configuration");
        assertThat(content).contains("## Security Configuration");
        assertThat(content).contains("## Nested Configuration");
        assertThat(content).contains("## Collection Configuration");
        assertThat(content).contains("app.server.port");
        assertThat(content).contains("app.database.url");
        assertThat(content).contains("app.security.jwt-secret");
        assertThat(content).contains("app.security.api-key");
    }

    @Test
    void markdownGroupsAreOrderedCorrectly(@TempDir Path tempDir) throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        TableMarkdownGenerator generator = new TableMarkdownGenerator();
        String markdown = generator.generate(bundle, GeneratorConfig.defaults(tempDir.resolve("out.md")));

        int serverIdx = markdown.indexOf("## Server Configuration");
        int dbIdx = markdown.indexOf("## Database Configuration");
        int secIdx = markdown.indexOf("## Security Configuration");

        assertThat(serverIdx).isLessThan(dbIdx);
        assertThat(dbIdx).isLessThan(secIdx);
    }

    @Test
    void generatedMarkdownMatchesExpectedOutput() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        TableMarkdownGenerator generator = new TableMarkdownGenerator();
        Path dummyOutput = Path.of("target/dummy.md");
        String actual = generator.generate(bundle, GeneratorConfig.defaults(dummyOutput));

        String expected = Files.readString(Path.of("src/test/resources/expected-output.md"));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void enrichedMetadataContainsNestedGroup() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.groups()).anyMatch(g -> g.displayName().equals("Nested Configuration"));
    }

    @Test
    void nestedPropertiesAreFlattenedToLeafKeys() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.nested.name"));
        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.nested.database.host"));
        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.nested.database.port"));
        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.nested.database.pool.size"));
        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.nested.database.pool.timeout"));
    }

    @Test
    void nestedParentKeysAreNotEmittedAsProperties() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).noneMatch(p -> p.name().equals("app.nested.database"));
        assertThat(bundle.properties()).noneMatch(p -> p.name().equals("app.nested.database.pool"));
    }

    @Test
    void nestedPropertyRetainsAnnotationMetadata() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.nested.database.host")
                        && p.required()
                        && p.description().equals("DB host"));
    }

    @Test
    void staticConstantsAreExcludedFromProperties() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).noneMatch(p ->
                p.name().contains("d-e-f-a-u-l-t") || p.name().contains("DEFAULT"));
    }

    @Test
    void collectionOfNestedExpandedWithBracketNotation() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.collection.endpoints[].url"));
        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.collection.endpoints[].timeout"));
    }

    @Test
    void mapOfNestedExpandedWithWildcardNotation() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.collection.data-sources.*.url"));
        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.collection.data-sources.*.username"));
    }

    @Test
    void nestedInsideMapValueIsRecursivelyExpanded() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p -> p.name().equals("app.collection.data-sources.*.pool.max-size"));
    }

    @Test
    void enumFieldShowsAllowedValues() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.collection.log-level")
                        && p.typeDisplay() != null
                        && p.typeDisplay().contains("TRACE")
                        && p.typeDisplay().contains("ERROR"));
    }

    @Test
    void simpleCollectionIsLeafWithProperDisplay() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.collection.tags")
                        && p.typeDisplay() != null
                        && p.typeDisplay().equals("List<String>"));
    }

    @Test
    void simpleMapIsLeafWithProperDisplay() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).anyMatch(p ->
                p.name().equals("app.collection.settings")
                        && p.typeDisplay() != null
                        && p.typeDisplay().equals("Map<String, String>"));
    }

    @Test
    void collectionAndMapParentKeysAreNotEmitted() throws IOException {
        MetadataReader reader = new MetadataReader();
        DocumentationBundle bundle = reader.readFromClassesDir(Path.of("target/classes"));

        assertThat(bundle.properties()).noneMatch(p -> p.name().equals("app.collection.endpoints"));
        assertThat(bundle.properties()).noneMatch(p -> p.name().equals("app.collection.data-sources"));
    }

    @Test
    void mavenPluginGeneratesMarkdownFile() {
        Path generatedFile = Path.of("target/configuration-properties.md");
        assertThat(generatedFile).exists();
        assertThat(generatedFile).isNotEmptyFile();
    }
}
