package io.github.springpropertiesmd.core.check;

import java.nio.file.Path;

public record DocumentationIssue(
        String rule,
        String propertyName,
        Path file,
        String message
) {
    public static DocumentationIssue property(String rule, String propertyName, String message) {
        return new DocumentationIssue(rule, propertyName, null, message);
    }

    public static DocumentationIssue file(String rule, Path file, String message) {
        return new DocumentationIssue(rule, null, file, message);
    }
}
