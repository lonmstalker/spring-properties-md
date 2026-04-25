package io.github.springpropertiesmd.core.check;

import java.util.List;

public record DocumentationCheckResult(List<DocumentationIssue> issues) {

    public DocumentationCheckResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public boolean passed() {
        return issues.stream().noneMatch(issue -> issue.severity() == DocumentationIssueSeverity.ERROR);
    }

    public String format() {
        if (issues.isEmpty()) {
            return "Configuration documentation checks passed.";
        }
        StringBuilder sb = new StringBuilder(passed()
                ? "Configuration documentation checks passed with warnings:"
                : "Configuration documentation checks failed:");
        for (DocumentationIssue issue : issues) {
            sb.append(System.lineSeparator()).append("- ");
            if (issue.severity() == DocumentationIssueSeverity.WARNING) {
                sb.append("Warning: ");
            }
            sb.append(issue.message());
        }
        return sb.toString();
    }
}
