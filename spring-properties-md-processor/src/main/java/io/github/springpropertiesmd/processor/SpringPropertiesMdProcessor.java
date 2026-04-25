package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyConditionMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
        "org.springframework.boot.context.properties.ConfigurationProperties",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnProperties"
})
public class SpringPropertiesMdProcessor extends AbstractProcessor {

    private PropertyExtractor extractor;
    private ConditionExtractor conditionExtractor;
    private MetadataJsonWriter jsonWriter;
    private final List<GroupMetadata> groups = new ArrayList<>();
    private final List<PropertyMetadata> properties = new ArrayList<>();
    private final Set<String> processedConfigurationElements = new HashSet<>();
    private boolean written;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.extractor = new PropertyExtractor(processingEnv);
        this.conditionExtractor = new ConditionExtractor(processingEnv);
        this.jsonWriter = new MetadataJsonWriter();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeMetadata();
            return false;
        }

        Set<Element> conditionElements = new LinkedHashSet<>();
        for (TypeElement annotation : annotations) {
            String annotationName = annotation.getQualifiedName().toString();
            if (ConditionExtractor.CONFIGURATION_PROPERTIES.equals(annotationName)) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                    processConfigurationPropertiesElement(element);
                }
            } else if (ConditionExtractor.CONDITIONAL_ON_PROPERTY.equals(annotationName)
                    || ConditionExtractor.CONDITIONAL_ON_PROPERTIES.equals(annotationName)) {
                conditionElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
        }
        conditionElements.forEach(conditionExtractor::process);

        return false;
    }

    private void writeMetadata() {
        List<PropertyConditionMetadata> conditions = conditionExtractor.classifiedConditions(groups, properties);
        if (written || (groups.isEmpty() && properties.isEmpty() && conditions.isEmpty())) {
            return;
        }
        written = true;
        DocumentationBundle bundle = new DocumentationBundle(groups, properties, conditions);
        try {
            jsonWriter.write(bundle, processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write enriched metadata: " + e.getMessage());
        }
    }

    private void processConfigurationPropertiesElement(Element element) {
        String key = sourceElement(element);
        if (!processedConfigurationElements.add(key)) {
            return;
        }
        String prefix = extractPrefix(element);
        if (element instanceof TypeElement typeElement) {
            addConfigurationType(typeElement, prefix);
            return;
        }
        if (element instanceof ExecutableElement method) {
            TypeElement returnType = toTypeElement(method.getReturnType());
            if (returnType == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "spring-properties-md cannot extract @ConfigurationProperties from method `"
                                + method.getSimpleName() + "` because its return type is not a declared type.",
                        method);
                return;
            }
            addConfigurationType(returnType, prefix);
        }
    }

    private void addConfigurationType(TypeElement typeElement, String prefix) {
        groups.add(extractor.extractGroup(typeElement, prefix));
        properties.addAll(extractor.extractProperties(typeElement, prefix));
        conditionExtractor.registerConfigurationType(typeElement, prefix);
    }

    private String extractPrefix(Element element) {
        var annotation = element.getAnnotationMirrors().stream()
                .filter(am -> ConditionExtractor.CONFIGURATION_PROPERTIES.equals(am.getAnnotationType().toString()))
                .findFirst()
                .orElse(null);
        if (annotation == null) {
            return "";
        }
        for (var entry : annotation.getElementValues().entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            if ("prefix".equals(key) || "value".equals(key)) {
                return entry.getValue().getValue().toString();
            }
        }
        return "";
    }

    private TypeElement toTypeElement(TypeMirror typeMirror) {
        Element element = processingEnv.getTypeUtils().asElement(typeMirror);
        return element instanceof TypeElement typeElement ? typeElement : null;
    }

    private String sourceElement(Element element) {
        if (element instanceof TypeElement typeElement) {
            return typeElement.getQualifiedName().toString();
        }
        if (element instanceof ExecutableElement method
                && method.getEnclosingElement() instanceof TypeElement owner) {
            return owner.getQualifiedName() + "#" + method.getSimpleName();
        }
        return element.toString();
    }
}
