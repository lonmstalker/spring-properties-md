package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("org.springframework.boot.context.properties.ConfigurationProperties")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class SpringPropertiesMdProcessor extends AbstractProcessor {

    private PropertyExtractor extractor;
    private MetadataJsonWriter jsonWriter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.extractor = new PropertyExtractor(processingEnv);
        this.jsonWriter = new MetadataJsonWriter();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        List<GroupMetadata> groups = new ArrayList<>();
        List<PropertyMetadata> properties = new ArrayList<>();

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement typeElement) {
                    String prefix = extractPrefix(typeElement);
                    groups.add(extractor.extractGroup(typeElement, prefix));
                    properties.addAll(extractor.extractProperties(typeElement, prefix));
                }
            }
        }

        if (!groups.isEmpty() || !properties.isEmpty()) {
            DocumentationBundle bundle = new DocumentationBundle(groups, properties);
            try {
                jsonWriter.write(bundle, processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to write enriched metadata: " + e.getMessage());
            }
        }

        return false;
    }

    private String extractPrefix(TypeElement typeElement) {
        try {
            var annotation = typeElement.getAnnotationMirrors().stream()
                    .filter(am -> am.getAnnotationType().toString()
                            .equals("org.springframework.boot.context.properties.ConfigurationProperties"))
                    .findFirst()
                    .orElse(null);

            if (annotation != null) {
                for (var entry : annotation.getElementValues().entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    if ("prefix".equals(key) || "value".equals(key)) {
                        return entry.getValue().getValue().toString();
                    }
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return "";
    }
}
