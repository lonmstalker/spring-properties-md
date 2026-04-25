package io.github.springpropertiesmd.core.check;

import java.nio.file.Path;

public record DocumentationIssue(
        String rule,
        String propertyName,
        Path file,
        String message,
        DocumentationIssueSeverity severity
) {
    public DocumentationIssue(String rule, String propertyName, Path file, String message) {
        this(rule, propertyName, file, message, DocumentationIssueSeverity.ERROR);
    }

    public static DocumentationIssue property(String rule, String propertyName, String message) {
        return new DocumentationIssue(rule, propertyName, null, message, DocumentationIssueSeverity.ERROR);
    }

    public static DocumentationIssue propertyWarning(String rule, String propertyName, String message) {
        return new DocumentationIssue(rule, propertyName, null, message, DocumentationIssueSeverity.WARNING);
    }

    public static DocumentationIssue file(String rule, Path file, String message) {
        return new DocumentationIssue(rule, null, file, message, DocumentationIssueSeverity.ERROR);
    }
}
