# Spring Properties MD

[![](https://jitpack.io/v/lonmstalker/spring-properties-md.svg)](https://jitpack.io/#lonmstalker/spring-properties-md)

Generate Markdown documentation from Spring Boot `@ConfigurationProperties` with enriched metadata.

## What it does

Spring Boot's `spring-boot-configuration-processor` generates basic `spring-configuration-metadata.json`. This project extends that with **custom annotations** carrying rich metadata (descriptions, requirements, examples, categories, deprecation info) and an **annotation processor** that produces an enriched JSON. Maven and Gradle plugins then turn that JSON into readable Markdown documentation.

## Architecture

```
spring-properties-md (root)
 ├── spring-properties-md-api              Annotations + model records
 ├── spring-properties-md-processor        Annotation processor → enriched-metadata.json
 ├── spring-properties-md-core             JSON reader + Markdown generator
 ├── spring-properties-md-maven-plugin     Maven plugin (generate-docs goal)
 ├── spring-properties-md-gradle-plugin    Gradle plugin (generatePropertyDocs task)
 └── spring-properties-md-integration-tests E2E tests
```

## Quick Start (Maven)

### 1. Add JitPack repository

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<pluginRepositories>
    <pluginRepository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </pluginRepository>
</pluginRepositories>
```

### 2. Add dependency

```xml
<dependency>
    <groupId>com.github.lonmstalker.spring-properties-md</groupId>
    <artifactId>spring-properties-md-api</artifactId>
    <version>v0.1.0</version>
</dependency>
```

### 3. Configure annotation processor

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.github.lonmstalker.spring-properties-md</groupId>
                <artifactId>spring-properties-md-processor</artifactId>
                <version>v0.1.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### 4. Add the Maven plugin

```xml
<plugin>
    <groupId>com.github.lonmstalker.spring-properties-md</groupId>
    <artifactId>spring-properties-md-maven-plugin</artifactId>
    <version>v0.1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-docs</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 5. Annotate your properties

```java
@ConfigurationProperties(prefix = "app.server")
@PropertyGroupDoc(
    displayName = "Server Configuration",
    description = "HTTP server settings",
    category = "Server",
    order = 1
)
public class ServerProperties {

    @PropertyDoc(description = "Server port number", required = Requirement.REQUIRED)
    @PropertyExample(value = "8080", description = "default")
    @PropertyExample(value = "443", description = "HTTPS")
    @PropertySince("1.0")
    private int port = 8080;

    @PropertyDoc(description = "Server bind address", required = Requirement.OPTIONAL)
    private String host = "localhost";
}
```

### 6. Build

```bash
mvn clean compile
```

Generated file: `target/configuration-properties.md`

## Example Output

```markdown
# Configuration Properties

## Table of Contents

- [Server Configuration](#server-configuration)
- [Database Configuration](#database-configuration)
- [Security Configuration](#security-configuration)

---

## Server Configuration

HTTP server settings

| Property | Type | Description | Default | Required | Constraints | Examples |
|----------|------|-------------|---------|----------|-------------|----------|
| `app.server.port` | `int` | Server port number |  | Yes |  | `8080` (default), `443` (HTTPS) |
| `app.server.host` | `String` | Server bind address |  | No |  | `0.0.0.0` (all interfaces) |
```

## Annotations

| Annotation | Target | Purpose |
|---|---|---|
| `@PropertyDoc` | FIELD, METHOD, PARAMETER, RECORD_COMPONENT | Main: description, required, profiles, sensitive, typeDisplay |
| `@PropertyGroupDoc` | TYPE | Class-level: displayName, description, category, order |
| `@PropertyExample` | FIELD, METHOD, PARAMETER, RECORD_COMPONENT | Repeatable: value + description |
| `@PropertyDeprecation` | FIELD, METHOD, TYPE, RECORD_COMPONENT | reason, replacedBy, since, removalVersion |
| `@PropertyCategory` | FIELD, METHOD, TYPE, RECORD_COMPONENT | category + subcategory |
| `@PropertySince` | FIELD, METHOD, TYPE, RECORD_COMPONENT | version |
| `@PropertySee` | FIELD, METHOD, TYPE, RECORD_COMPONENT | Repeatable: cross-reference |
| `@PropertyCustomMetadata` | FIELD, METHOD, TYPE, RECORD_COMPONENT | Repeatable: key-value pairs |

Property-level annotations are read from fields, record components, JavaBean getters, and constructor parameters.

## Maven Plugin Configuration

| Parameter | Default | Description |
|---|---|---|
| `outputFile` | `${project.build.directory}/configuration-properties.md` | Output file path |
| `outputDirectory` | `${project.build.directory}/configuration-properties` | Output directory for `PER_GROUP` and `PER_CATEGORY` |
| `title` | `Configuration Properties` | Document title |
| `outputStyle` | `SINGLE_FILE` | `SINGLE_FILE`, `PER_GROUP`, `PER_CATEGORY` |
| `sensitiveMode` | `REDACT` | `SHOW`, `REDACT`, `OMIT` |
| `includeTableOfContents` | `true` | Include TOC |
| `includeDeprecated` | `true` | Include deprecated properties |
| `includeValidation` | `true` | Show validation constraints |
| `includeExamples` | `true` | Show examples |
| `includeCustomMetadata` | `false` | Show custom metadata |

### Documentation Checks

The Maven plugin also provides an explicit quality gate goal:

```bash
mvn spring-properties-md:check-docs
```

By default it fails on missing descriptions, sensitive defaults/examples, deprecated properties without replacements, required properties without examples, and duplicate property names. To also verify committed documentation is current, enable `failIfGeneratedDocsChanged`.

## Quick Start (Gradle)

```groovy
plugins {
    id 'io.github.spring-properties-md'
}

springPropertiesMd {
    title = 'My App Configuration'
    outputFile = 'docs/configuration.md'
    outputDirectory = 'docs/configuration'
    sensitiveMode = 'REDACT'
}
```

```bash
./gradlew generatePropertyDocs
./gradlew checkPropertyDocs
```

## Requirements

- Java 21+
- Spring Boot 3.x (for `@ConfigurationProperties`)

## Building

```bash
mvn clean install
```
