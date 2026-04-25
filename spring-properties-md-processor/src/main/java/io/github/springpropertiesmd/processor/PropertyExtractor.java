package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.annotation.*;
import io.github.springpropertiesmd.api.model.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.function.Function;

public class PropertyExtractor {

    private final TypeResolver typeResolver;
    private final ValidationConstraintExtractor validationExtractor;
    private final PropertyNameResolver nameResolver;
    private final ProcessingEnvironment processingEnv;

    private record PropertyCandidate(
            String propertyName,
            TypeMirror type,
            Element element,
            TypeElement ownerType,
            int priority
    ) {
    }

    public PropertyExtractor(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.typeResolver = new TypeResolver(processingEnv);
        this.validationExtractor = new ValidationConstraintExtractor();
        this.nameResolver = new PropertyNameResolver();
    }

    public GroupMetadata extractGroup(TypeElement typeElement, String prefix) {
        warnUnsupportedTypeAnnotations(typeElement);
        PropertyGroupDoc groupDoc = typeElement.getAnnotation(PropertyGroupDoc.class);
        PropertyCategory category = typeElement.getAnnotation(PropertyCategory.class);

        String displayName = groupDoc != null && !groupDoc.displayName().isEmpty()
                ? groupDoc.displayName() : typeElement.getSimpleName().toString();
        String description = groupDoc != null ? groupDoc.description() : "";
        String cat = category != null ? category.value()
                : (groupDoc != null ? groupDoc.category() : "");
        int order = groupDoc != null ? groupDoc.order() : Integer.MAX_VALUE;

        return new GroupMetadata(
                prefix,
                displayName,
                description,
                typeElement.getQualifiedName().toString(),
                cat,
                order
        );
    }

    public List<PropertyMetadata> extractProperties(TypeElement typeElement, String prefix) {
        return extractProperties(typeElement, prefix, prefix, new HashSet<>());
    }

    private List<PropertyMetadata> extractProperties(TypeElement typeElement, String prefix,
                                                     String groupPrefix, Set<String> visited) {
        if (!visited.add(typeElement.getQualifiedName().toString())) {
            return List.of();
        }

        List<PropertyMetadata> properties = new ArrayList<>();
        Map<String, List<PropertyCandidate>> candidatesByName = collectCandidates(typeElement);

        for (List<PropertyCandidate> candidates : candidatesByName.values()) {
            PropertyCandidate primary = primaryCandidate(candidates);

            PropertyType propertyType = typeResolver.resolve(primary.type());

            switch (propertyType) {
                case PropertyType.NestedType nt -> {
                    String nestedPrefix = nameResolver.resolve(prefix, primary.propertyName());
                    TypeElement nested = typeResolver.toTypeElement(primary.type());
                    if (nested != null) {
                        properties.addAll(extractProperties(nested, nestedPrefix, groupPrefix, new HashSet<>(visited)));
                    }
                }
                case PropertyType.CollectionType ct ->
                    properties.addAll(handleCollectionType(ct, primary, candidates, prefix, groupPrefix, visited));
                case PropertyType.MapType mt ->
                    properties.addAll(handleMapType(mt, primary, candidates, prefix, groupPrefix, visited));
                case PropertyType.EnumType et ->
                    properties.add(extractPropertyWithTypeDisplay(candidates, prefix, groupPrefix, formatEnumTypeDisplay(et)));
                case PropertyType.SimpleType st ->
                    properties.add(extractProperty(candidates, prefix, groupPrefix));
            }
        }

        return properties;
    }

    private Map<String, List<PropertyCandidate>> collectCandidates(TypeElement typeElement) {
        Map<String, List<PropertyCandidate>> candidates = new LinkedHashMap<>();
        Set<String> processedFields = new HashSet<>();
        List<ExecutableElement> constructors = new ArrayList<>();

        for (Element enclosed : typeElement.getEnclosedElements()) {
            switch (enclosed.getKind()) {
                case FIELD -> {
                    if (enclosed.getModifiers().contains(Modifier.STATIC)) {
                        continue;
                    }
                    addCandidate(candidates, new PropertyCandidate(
                            enclosed.getSimpleName().toString(),
                            enclosed.asType(),
                            enclosed,
                            typeElement,
                            3
                    ), processedFields);
                }
                case RECORD_COMPONENT -> addCandidate(candidates, new PropertyCandidate(
                        enclosed.getSimpleName().toString(),
                        enclosed.asType(),
                        enclosed,
                        typeElement,
                        3
                ), processedFields);
                case METHOD -> collectMethodCandidate(typeElement, (ExecutableElement) enclosed, candidates);
                case CONSTRUCTOR -> constructors.add((ExecutableElement) enclosed);
                default -> {
                    if (hasPropertyMetadataAnnotation(enclosed)) {
                        warn(enclosed, "spring-properties-md property annotations are ignored on unsupported element "
                                + enclosed.getKind() + ".");
                    }
                }
            }
        }
        for (ExecutableElement constructor : constructors) {
            collectConstructorCandidates(typeElement, constructor, candidates);
        }

        return candidates;
    }

    private void collectMethodCandidate(TypeElement typeElement, ExecutableElement method,
                                        Map<String, List<PropertyCandidate>> candidates) {
        String propertyName = getterPropertyName(method);
        if (propertyName == null) {
            if (hasPropertyMetadataAnnotation(method) && !isRecordAccessor(typeElement, method)) {
                warn(method, "spring-properties-md property annotations are ignored on unsupported method `"
                        + method.getSimpleName() + "`.");
            }
            return;
        }
        candidates.computeIfAbsent(propertyName, ignored -> new ArrayList<>())
                .add(new PropertyCandidate(propertyName, method.getReturnType(), method, typeElement, 2));
    }

    private void collectConstructorCandidates(TypeElement typeElement, ExecutableElement constructor,
                                              Map<String, List<PropertyCandidate>> candidates) {
        for (VariableElement parameter : constructor.getParameters()) {
            String name = parameter.getSimpleName().toString();
            if (name.startsWith("$")) {
                continue;
            }
            if (!hasPropertyMetadataAnnotation(parameter) && !candidates.containsKey(name)) {
                continue;
            }
            candidates.computeIfAbsent(name, ignored -> new ArrayList<>())
                    .add(new PropertyCandidate(name, parameter.asType(), parameter, typeElement, 1));
        }
    }

    private void addCandidate(Map<String, List<PropertyCandidate>> candidates, PropertyCandidate candidate,
                              Set<String> processedFields) {
        if (candidate.propertyName().startsWith("$") || !processedFields.add(candidate.propertyName())) {
            return;
        }
        candidates.computeIfAbsent(candidate.propertyName(), ignored -> new ArrayList<>()).add(candidate);
    }

    private PropertyCandidate primaryCandidate(List<PropertyCandidate> candidates) {
        return candidates.stream()
                .max(Comparator.comparingInt(PropertyCandidate::priority))
                .orElseThrow();
    }

    private String getterPropertyName(ExecutableElement method) {
        if (!method.getParameters().isEmpty() || method.getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        String name = method.getSimpleName().toString();
        if (name.equals("getClass")) {
            return null;
        }
        if (name.startsWith("get") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return decapitalize(name.substring(2));
        }
        return null;
    }

    private boolean isRecordAccessor(TypeElement typeElement, ExecutableElement method) {
        if (!method.getParameters().isEmpty()) {
            return false;
        }
        String methodName = method.getSimpleName().toString();
        return typeElement.getEnclosedElements().stream()
                .anyMatch(element -> element.getKind() == ElementKind.RECORD_COMPONENT
                        && element.getSimpleName().toString().equals(methodName));
    }

    private String decapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private List<PropertyMetadata> handleCollectionType(PropertyType.CollectionType ct, PropertyCandidate primary,
                                                         List<PropertyCandidate> candidates,
                                                         String prefix, String groupPrefix,
                                                         Set<String> visited) {
        if (ct.elementType() instanceof PropertyType.NestedType) {
            String nestedPrefix = nameResolver.resolve(prefix, primary.propertyName()) + "[]";
            TypeMirror elementMirror = typeResolver.getElementTypeMirror(primary.type());
            TypeElement nested = elementMirror != null ? typeResolver.toTypeElement(elementMirror) : null;
            if (nested != null) {
                return extractProperties(nested, nestedPrefix, groupPrefix, new HashSet<>(visited));
            }
        }
        return List.of(extractPropertyWithTypeDisplay(candidates, prefix, groupPrefix, typeDisplayName(ct)));
    }

    private List<PropertyMetadata> handleMapType(PropertyType.MapType mt, PropertyCandidate primary,
                                                   List<PropertyCandidate> candidates,
                                                   String prefix, String groupPrefix,
                                                   Set<String> visited) {
        if (mt.valueType() instanceof PropertyType.NestedType) {
            String nestedPrefix = nameResolver.resolve(prefix, primary.propertyName()) + ".*";
            TypeMirror valueMirror = typeResolver.getElementTypeMirror(primary.type());
            TypeElement nested = valueMirror != null ? typeResolver.toTypeElement(valueMirror) : null;
            if (nested != null) {
                return extractProperties(nested, nestedPrefix, groupPrefix, new HashSet<>(visited));
            }
        }
        return List.of(extractPropertyWithTypeDisplay(candidates, prefix, groupPrefix, typeDisplayName(mt)));
    }

    private PropertyMetadata extractPropertyWithTypeDisplay(List<PropertyCandidate> candidates, String prefix,
                                                             String groupPrefix, String computedTypeDisplay) {
        PropertyMetadata base = extractProperty(candidates, prefix, groupPrefix);
        if (base.typeDisplay() != null && !base.typeDisplay().isEmpty()) {
            return base;
        }
        return new PropertyMetadata(
                base.name(), base.type(), computedTypeDisplay, base.description(),
                base.defaultValue(), base.required(), base.sensitive(), base.profiles(),
                base.deprecation(), base.examples(), base.constraints(),
                base.category(), base.subcategory(), base.since(),
                base.seeAlso(), base.customMetadata(), base.sourceType(), base.groupName()
        );
    }

    private String typeDisplayName(PropertyType type) {
        return switch (type) {
            case PropertyType.SimpleType st -> st.displayName();
            case PropertyType.EnumType et -> formatEnumTypeDisplay(et);
            case PropertyType.CollectionType ct -> {
                String collectionSimple = ct.collectionFqcn().substring(ct.collectionFqcn().lastIndexOf('.') + 1);
                yield collectionSimple + "<" + typeDisplayName(ct.elementType()) + ">";
            }
            case PropertyType.MapType mt -> "Map<" + typeDisplayName(mt.keyType()) + ", " + typeDisplayName(mt.valueType()) + ">";
            case PropertyType.NestedType nt -> nt.fqcn().substring(nt.fqcn().lastIndexOf('.') + 1);
        };
    }

    private String formatEnumTypeDisplay(PropertyType.EnumType et) {
        String simpleName = et.fqcn().substring(et.fqcn().lastIndexOf('.') + 1);
        return simpleName + " (" + String.join(", ", et.allowedValues()) + ")";
    }

    private PropertyMetadata extractProperty(List<PropertyCandidate> candidates, String prefix, String groupPrefix) {
        List<PropertyCandidate> ordered = candidates.stream()
                .sorted(Comparator.comparingInt(PropertyCandidate::priority).reversed())
                .toList();
        PropertyCandidate primary = ordered.getFirst();
        String propertyName = nameResolver.resolve(prefix, primary.propertyName());

        List<PropertyDoc> docs = annotations(ordered, element -> element.getAnnotation(PropertyDoc.class));
        PropertyDeprecation deprecation = firstAnnotation(ordered, element -> element.getAnnotation(PropertyDeprecation.class));
        PropertyCategory category = firstAnnotation(ordered, element -> element.getAnnotation(PropertyCategory.class));
        PropertySince since = firstAnnotation(ordered, element -> element.getAnnotation(PropertySince.class));

        String description = docs.stream()
                .map(PropertyDoc::description)
                .filter(value -> !value.isEmpty())
                .findFirst()
                .orElse("");
        boolean required = docs.stream().anyMatch(doc -> doc.required() == Requirement.REQUIRED);
        boolean sensitive = docs.stream().anyMatch(PropertyDoc::sensitive);
        List<String> profiles = docs.stream()
                .map(doc -> List.of(doc.profiles()))
                .filter(values -> !values.isEmpty())
                .findFirst()
                .orElse(List.of());
        String typeDisplay = docs.stream()
                .map(PropertyDoc::typeDisplay)
                .filter(value -> !value.isEmpty())
                .findFirst()
                .orElse(null);

        DeprecationInfo deprecationInfo = deprecation != null
                ? new DeprecationInfo(deprecation.reason(), deprecation.replacedBy(),
                deprecation.since(), deprecation.removalVersion())
                : firstSpringBootDeprecation(ordered);

        List<ExampleValue> examples = firstNonEmpty(ordered, this::extractExamples);
        List<ValidationConstraint> constraints = firstNonEmpty(ordered, candidate -> validationExtractor.extract(candidate.element()));
        List<String> seeAlso = firstNonEmpty(ordered, candidate -> extractSeeAlso(candidate.element()));
        Map<String, String> customMetadata = firstNonEmptyMap(ordered, candidate -> extractCustomMetadata(candidate.element()));

        String cat = category != null ? category.value() : null;
        String subcat = category != null ? category.subcategory() : null;
        String sinceVersion = since != null ? since.value() : null;

        Object constantValue = ordered.stream()
                .map(PropertyCandidate::element)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(VariableElement::getConstantValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        String defaultValue = constantValue != null ? constantValue.toString() : null;

        String typeFqcn = primary.type().toString();

        return new PropertyMetadata(
                propertyName,
                typeFqcn,
                typeDisplay,
                description,
                defaultValue,
                required,
                sensitive,
                profiles,
                deprecationInfo,
                examples,
                constraints,
                cat,
                subcat,
                sinceVersion,
                seeAlso,
                customMetadata,
                primary.ownerType().getQualifiedName().toString(),
                groupPrefix
        );
    }

    private <T> List<T> annotations(List<PropertyCandidate> candidates, Function<Element, T> extractor) {
        List<T> result = new ArrayList<>();
        for (PropertyCandidate candidate : candidates) {
            T value = extractor.apply(candidate.element());
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private <T> T firstAnnotation(List<PropertyCandidate> candidates, Function<Element, T> extractor) {
        for (PropertyCandidate candidate : candidates) {
            T value = extractor.apply(candidate.element());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private <T> List<T> firstNonEmpty(List<PropertyCandidate> candidates,
                                      Function<PropertyCandidate, List<T>> extractor) {
        for (PropertyCandidate candidate : candidates) {
            List<T> values = extractor.apply(candidate);
            if (values != null && !values.isEmpty()) {
                return values;
            }
        }
        return List.of();
    }

    private Map<String, String> firstNonEmptyMap(List<PropertyCandidate> candidates,
                                                 Function<PropertyCandidate, Map<String, String>> extractor) {
        for (PropertyCandidate candidate : candidates) {
            Map<String, String> values = extractor.apply(candidate);
            if (values != null && !values.isEmpty()) {
                return values;
            }
        }
        return Map.of();
    }

    private DeprecationInfo firstSpringBootDeprecation(List<PropertyCandidate> candidates) {
        for (PropertyCandidate candidate : candidates) {
            DeprecationInfo deprecation = springBootDeprecation(candidate.element());
            if (deprecation != null) {
                return deprecation;
            }
        }
        return null;
    }

    private DeprecationInfo springBootDeprecation(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (!mirror.getAnnotationType().toString()
                    .equals("org.springframework.boot.context.properties.DeprecatedConfigurationProperty")) {
                continue;
            }
            Map<String, String> values = new HashMap<>();
            for (var entry : mirror.getElementValues().entrySet()) {
                values.put(entry.getKey().getSimpleName().toString(), entry.getValue().getValue().toString());
            }
            return new DeprecationInfo(values.getOrDefault("reason", ""),
                    values.getOrDefault("replacement", ""), null, null);
        }
        return null;
    }

    private List<ExampleValue> extractExamples(Element element) {
        PropertyExample[] examples = element.getAnnotationsByType(PropertyExample.class);
        if (examples.length == 0) {
            return List.of();
        }
        List<ExampleValue> result = new ArrayList<>();
        for (PropertyExample ex : examples) {
            result.add(new ExampleValue(ex.value(), ex.description()));
        }
        return result;
    }

    private List<ExampleValue> extractExamples(PropertyCandidate candidate) {
        return extractExamples(candidate.element());
    }

    private List<String> extractSeeAlso(Element element) {
        PropertySee[] sees = element.getAnnotationsByType(PropertySee.class);
        if (sees.length == 0) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (PropertySee see : sees) {
            result.add(see.value());
        }
        return result;
    }

    private Map<String, String> extractCustomMetadata(Element element) {
        PropertyCustomMetadata[] entries = element.getAnnotationsByType(PropertyCustomMetadata.class);
        if (entries.length == 0) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (PropertyCustomMetadata entry : entries) {
            result.put(entry.key(), entry.value());
        }
        return result;
    }

    private boolean hasPropertyMetadataAnnotation(Element element) {
        return element.getAnnotation(PropertyDoc.class) != null
                || element.getAnnotationsByType(PropertyExample.class).length > 0
                || element.getAnnotation(PropertyDeprecation.class) != null
                || element.getAnnotation(PropertyCategory.class) != null
                || element.getAnnotation(PropertySince.class) != null
                || element.getAnnotationsByType(PropertySee.class).length > 0
                || element.getAnnotationsByType(PropertyCustomMetadata.class).length > 0;
    }

    private void warnUnsupportedTypeAnnotations(TypeElement typeElement) {
        if (typeElement.getAnnotation(PropertyDeprecation.class) != null
                || typeElement.getAnnotation(PropertySince.class) != null
                || typeElement.getAnnotationsByType(PropertySee.class).length > 0
                || typeElement.getAnnotationsByType(PropertyCustomMetadata.class).length > 0) {
            warn(typeElement, "spring-properties-md property-level annotations are ignored on type `"
                    + typeElement.getSimpleName() + "`.");
        }
    }

    private void warn(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
    }
}
