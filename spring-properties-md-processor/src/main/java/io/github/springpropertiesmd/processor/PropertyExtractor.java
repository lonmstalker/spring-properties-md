package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.annotation.*;
import io.github.springpropertiesmd.api.model.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class PropertyExtractor {

    private final TypeResolver typeResolver;
    private final ValidationConstraintExtractor validationExtractor;
    private final PropertyNameResolver nameResolver;

    public PropertyExtractor(ProcessingEnvironment processingEnv) {
        this.typeResolver = new TypeResolver(processingEnv);
        this.validationExtractor = new ValidationConstraintExtractor();
        this.nameResolver = new PropertyNameResolver();
    }

    public GroupMetadata extractGroup(TypeElement typeElement, String prefix) {
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

    private static boolean isPropertyElement(Element e) {
        if (e.getKind() == ElementKind.RECORD_COMPONENT) {
            return true;
        }
        return e.getKind() == ElementKind.FIELD && !e.getModifiers().contains(Modifier.STATIC);
    }

    private List<PropertyMetadata> extractProperties(TypeElement typeElement, String prefix,
                                                     String groupPrefix, Set<String> visited) {
        if (!visited.add(typeElement.getQualifiedName().toString())) {
            return List.of();
        }

        List<PropertyMetadata> properties = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (!isPropertyElement(enclosed)) {
                continue;
            }
            String name = enclosed.getSimpleName().toString();
            if (name.startsWith("$") || !processed.add(name)) {
                continue;
            }

            PropertyType propertyType = typeResolver.resolve(enclosed.asType());

            switch (propertyType) {
                case PropertyType.NestedType nt -> {
                    String nestedPrefix = nameResolver.resolve(prefix, name);
                    TypeElement nested = typeResolver.toTypeElement(enclosed.asType());
                    if (nested != null) {
                        properties.addAll(extractProperties(nested, nestedPrefix, groupPrefix, new HashSet<>(visited)));
                    }
                }
                case PropertyType.CollectionType ct ->
                    properties.addAll(handleCollectionType(ct, enclosed, prefix, groupPrefix, visited, name));
                case PropertyType.MapType mt ->
                    properties.addAll(handleMapType(mt, enclosed, prefix, groupPrefix, visited, name));
                case PropertyType.EnumType et ->
                    properties.add(extractPropertyWithTypeDisplay(enclosed, prefix, groupPrefix, typeElement, formatEnumTypeDisplay(et)));
                case PropertyType.SimpleType st ->
                    properties.add(extractProperty(enclosed, prefix, groupPrefix, typeElement));
            }
        }

        return properties;
    }

    private List<PropertyMetadata> handleCollectionType(PropertyType.CollectionType ct, Element enclosed,
                                                         String prefix, String groupPrefix,
                                                         Set<String> visited, String name) {
        if (ct.elementType() instanceof PropertyType.NestedType) {
            String nestedPrefix = nameResolver.resolve(prefix, name) + "[]";
            TypeMirror elementMirror = typeResolver.getElementTypeMirror(enclosed.asType());
            TypeElement nested = elementMirror != null ? typeResolver.toTypeElement(elementMirror) : null;
            if (nested != null) {
                return extractProperties(nested, nestedPrefix, groupPrefix, new HashSet<>(visited));
            }
        }
        return List.of(extractPropertyWithTypeDisplay(enclosed, prefix, groupPrefix,
                (TypeElement) enclosed.getEnclosingElement(), typeDisplayName(ct)));
    }

    private List<PropertyMetadata> handleMapType(PropertyType.MapType mt, Element enclosed,
                                                   String prefix, String groupPrefix,
                                                   Set<String> visited, String name) {
        if (mt.valueType() instanceof PropertyType.NestedType) {
            String nestedPrefix = nameResolver.resolve(prefix, name) + ".*";
            TypeMirror valueMirror = typeResolver.getElementTypeMirror(enclosed.asType());
            TypeElement nested = valueMirror != null ? typeResolver.toTypeElement(valueMirror) : null;
            if (nested != null) {
                return extractProperties(nested, nestedPrefix, groupPrefix, new HashSet<>(visited));
            }
        }
        return List.of(extractPropertyWithTypeDisplay(enclosed, prefix, groupPrefix,
                (TypeElement) enclosed.getEnclosingElement(), typeDisplayName(mt)));
    }

    private PropertyMetadata extractPropertyWithTypeDisplay(Element field, String prefix, String groupPrefix,
                                                             TypeElement ownerType, String computedTypeDisplay) {
        PropertyMetadata base = extractProperty(field, prefix, groupPrefix, ownerType);
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

    private PropertyMetadata extractProperty(Element field, String prefix, String groupPrefix, TypeElement ownerType) {
        String fieldName = field.getSimpleName().toString();
        String propertyName = nameResolver.resolve(prefix, fieldName);

        PropertyDoc doc = field.getAnnotation(PropertyDoc.class);
        PropertyDeprecation deprecation = field.getAnnotation(PropertyDeprecation.class);
        PropertyCategory category = field.getAnnotation(PropertyCategory.class);
        PropertySince since = field.getAnnotation(PropertySince.class);

        String description = doc != null ? doc.description() : "";
        boolean required = doc != null && doc.required() == Requirement.REQUIRED;
        boolean sensitive = doc != null && doc.sensitive();
        List<String> profiles = doc != null ? List.of(doc.profiles()) : List.of();
        String typeDisplay = doc != null && !doc.typeDisplay().isEmpty() ? doc.typeDisplay() : null;

        DeprecationInfo deprecationInfo = deprecation != null
                ? new DeprecationInfo(deprecation.reason(), deprecation.replacedBy(),
                deprecation.since(), deprecation.removalVersion())
                : null;

        List<ExampleValue> examples = extractExamples(field);
        List<ValidationConstraint> constraints = validationExtractor.extract(field);
        List<String> seeAlso = extractSeeAlso(field);
        Map<String, String> customMetadata = extractCustomMetadata(field);

        String cat = category != null ? category.value() : null;
        String subcat = category != null ? category.subcategory() : null;
        String sinceVersion = since != null ? since.value() : null;

        Object constantValue = (field instanceof VariableElement ve) ? ve.getConstantValue() : null;
        String defaultValue = constantValue != null ? constantValue.toString() : null;

        String typeFqcn = field.asType().toString();

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
                ownerType.getQualifiedName().toString(),
                groupPrefix
        );
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
}
