package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.PropertyType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeResolver {

    private static final Set<String> COLLECTION_TYPES = Set.of(
            "java.util.List", "java.util.Set", "java.util.Collection"
    );

    private static final Map<String, String> PRIMITIVE_DISPLAY_NAMES = Map.ofEntries(
            Map.entry("java.lang.String", "String"),
            Map.entry("java.lang.Integer", "Integer"),
            Map.entry("java.lang.Long", "Long"),
            Map.entry("java.lang.Boolean", "Boolean"),
            Map.entry("java.lang.Double", "Double"),
            Map.entry("java.lang.Float", "Float"),
            Map.entry("java.lang.Short", "Short"),
            Map.entry("java.lang.Byte", "Byte"),
            Map.entry("java.lang.Character", "Character"),
            Map.entry("int", "int"),
            Map.entry("long", "long"),
            Map.entry("boolean", "boolean"),
            Map.entry("double", "double"),
            Map.entry("float", "float"),
            Map.entry("short", "short"),
            Map.entry("byte", "byte"),
            Map.entry("char", "char"),
            Map.entry("java.time.Duration", "Duration"),
            Map.entry("java.time.Period", "Period"),
            Map.entry("java.time.Instant", "Instant"),
            Map.entry("java.time.LocalDate", "LocalDate"),
            Map.entry("java.time.LocalTime", "LocalTime"),
            Map.entry("java.time.LocalDateTime", "LocalDateTime")
    );

    private final ProcessingEnvironment processingEnv;

    public TypeResolver(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public PropertyType resolve(TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) {
            String name = typeMirror.toString();
            return new PropertyType.SimpleType(name, PRIMITIVE_DISPLAY_NAMES.getOrDefault(name, name));
        }

        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return new PropertyType.SimpleType(typeMirror.toString(), typeMirror.toString());
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        String fqcn = typeElement.getQualifiedName().toString();

        if (fqcn.equals("java.util.Map") && declaredType.getTypeArguments().size() == 2) {
            PropertyType keyType = resolve(declaredType.getTypeArguments().get(0));
            PropertyType valueType = resolve(declaredType.getTypeArguments().get(1));
            return new PropertyType.MapType(keyType, valueType);
        }

        if (COLLECTION_TYPES.contains(fqcn) && !declaredType.getTypeArguments().isEmpty()) {
            PropertyType elementType = resolve(declaredType.getTypeArguments().getFirst());
            return new PropertyType.CollectionType(fqcn, elementType);
        }

        if (typeElement.getKind() == ElementKind.ENUM) {
            List<String> values = typeElement.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
                    .map(e -> e.getSimpleName().toString())
                    .toList();
            return new PropertyType.EnumType(fqcn, values);
        }

        String displayName = PRIMITIVE_DISPLAY_NAMES.get(fqcn);
        if (displayName != null) {
            return new PropertyType.SimpleType(fqcn, displayName);
        }

        if (isNestedConfigurationProperties(typeElement)) {
            return new PropertyType.NestedType(fqcn, typeElement.getSimpleName().toString().toLowerCase());
        }

        return new PropertyType.SimpleType(fqcn, typeElement.getSimpleName().toString());
    }

    private boolean isNestedConfigurationProperties(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .anyMatch(e -> e.getKind() == ElementKind.FIELD);
    }
}
