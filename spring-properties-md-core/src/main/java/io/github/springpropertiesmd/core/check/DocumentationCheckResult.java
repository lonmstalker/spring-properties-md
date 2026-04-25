package io.github.springpropertiesmd.core.check;

import java.util.List;

public record DocumentationCheckResult(List<DocumentationIssue> issues) {

    public DocumentationCheckResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public boolean passed() {
        return issues.isEmpty();
    }

    public String format() {
        if (passed()) {
            return "Configuration documentation checks passed.";
        }
        StringBuilder sb = new StringBuilder("Configuration documentation checks failed:");
        for (DocumentationIssue issue : issues) {
            sb.append(System.lineSeparator()).append("- ").append(issue.message());
        }
        return sb.toString();
    }
}
