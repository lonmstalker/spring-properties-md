package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.ValidationConstraint;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValidationConstraintExtractor {

    private static final Map<String, ConstraintFormatter> KNOWN_CONSTRAINTS = Map.ofEntries(
            Map.entry("jakarta.validation.constraints.NotNull", (ann) -> new ValidationConstraint("NotNull", "must not be null")),
            Map.entry("jakarta.validation.constraints.NotBlank", (ann) -> new ValidationConstraint("NotBlank", "must not be blank")),
            Map.entry("jakarta.validation.constraints.NotEmpty", (ann) -> new ValidationConstraint("NotEmpty", "must not be empty")),
            Map.entry("jakarta.validation.constraints.Size", ValidationConstraintExtractor::formatSize),
            Map.entry("jakarta.validation.constraints.Min", ValidationConstraintExtractor::formatMin),
            Map.entry("jakarta.validation.constraints.Max", ValidationConstraintExtractor::formatMax),
            Map.entry("jakarta.validation.constraints.Pattern", ValidationConstraintExtractor::formatPattern),
            Map.entry("jakarta.validation.constraints.Email", (ann) -> new ValidationConstraint("Email", "must be a valid email")),
            Map.entry("jakarta.validation.constraints.Positive", (ann) -> new ValidationConstraint("Positive", "must be positive")),
            Map.entry("jakarta.validation.constraints.PositiveOrZero", (ann) -> new ValidationConstraint("PositiveOrZero", "must be positive or zero")),
            Map.entry("jakarta.validation.constraints.Negative", (ann) -> new ValidationConstraint("Negative", "must be negative")),
            Map.entry("jakarta.validation.constraints.NegativeOrZero", (ann) -> new ValidationConstraint("NegativeOrZero", "must be negative or zero"))
    );

    public List<ValidationConstraint> extract(Element element) {
        List<ValidationConstraint> constraints = new ArrayList<>();

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            ConstraintFormatter formatter = KNOWN_CONSTRAINTS.get(annotationName);
            if (formatter != null) {
                constraints.add(formatter.format(annotationMirror));
            }
        }

        return List.copyOf(constraints);
    }

    private static ValidationConstraint formatSize(AnnotationMirror annotation) {
        Map<String, Object> values = extractValues(annotation);
        Object min = values.get("min");
        Object max = values.get("max");
        String desc;
        if (min != null && max != null) {
            desc = "size must be between %s and %s".formatted(min, max);
        } else if (min != null) {
            desc = "size must be at least %s".formatted(min);
        } else if (max != null) {
            desc = "size must be at most %s".formatted(max);
        } else {
            desc = "size constraint";
        }
        return new ValidationConstraint("Size", desc);
    }

    private static ValidationConstraint formatMin(AnnotationMirror annotation) {
        Map<String, Object> values = extractValues(annotation);
        return new ValidationConstraint("Min", "must be at least %s".formatted(values.getOrDefault("value", "?")));
    }

    private static ValidationConstraint formatMax(AnnotationMirror annotation) {
        Map<String, Object> values = extractValues(annotation);
        return new ValidationConstraint("Max", "must be at most %s".formatted(values.getOrDefault("value", "?")));
    }

    private static ValidationConstraint formatPattern(AnnotationMirror annotation) {
        Map<String, Object> values = extractValues(annotation);
        return new ValidationConstraint("Pattern", "must match pattern: %s".formatted(values.getOrDefault("regexp", "?")));
    }

    private static Map<String, Object> extractValues(AnnotationMirror annotation) {
        var result = new java.util.HashMap<String, Object>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotation.getElementValues().entrySet()) {
            result.put(entry.getKey().getSimpleName().toString(), entry.getValue().getValue());
        }
        return result;
    }

    @FunctionalInterface
    private interface ConstraintFormatter {
        ValidationConstraint format(AnnotationMirror annotation);
    }
}
