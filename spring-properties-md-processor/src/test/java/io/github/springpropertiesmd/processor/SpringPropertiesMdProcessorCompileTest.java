package io.github.springpropertiesmd.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import io.github.springpropertiesmd.api.model.DocumentationBundle;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class SpringPropertiesMdProcessorCompileTest {

    private final MetadataJsonWriter writer = new MetadataJsonWriter();

    @Test
    void extractsPropertyAnnotationsFromGetterMethods() throws IOException {
        Compilation compilation = compile("""
                package com.example;

                import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                import io.github.springpropertiesmd.api.annotation.PropertyExample;
                import org.springframework.boot.context.properties.ConfigurationProperties;

                @ConfigurationProperties(prefix = "app")
                class GetterProperties {
                    private String mode;

                    @PropertyDoc(description = "Execution mode")
                    @PropertyExample(value = "sync", description = "sync mode")
                    public String getMode() {
                        return mode;
                    }
                }
                """);

        assertThat(compilation).succeeded();
        DocumentationBundle bundle = generatedBundle(compilation);
        assertThat(bundle.properties()).anyMatch(property ->
                property.name().equals("app.mode")
                        && property.description().equals("Execution mode")
                        && property.examples().get(0).value().equals("sync"));
    }

    @Test
    void extractsPropertyAnnotationsFromConstructorParameters() throws IOException {
        Compilation compilation = compile("""
                package com.example;

                import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                import io.github.springpropertiesmd.api.annotation.Requirement;
                import org.springframework.boot.context.properties.ConfigurationProperties;

                @ConfigurationProperties(prefix = "app")
                class ConstructorProperties {
                    private final String token;

                    ConstructorProperties(@PropertyDoc(description = "Access token", required = Requirement.REQUIRED, sensitive = true) String token) {
                        this.token = token;
                    }

                    public String getToken() {
                        return token;
                    }
                }
                """);

        assertThat(compilation).succeeded();
        DocumentationBundle bundle = generatedBundle(compilation);
        assertThat(bundle.properties()).anyMatch(property ->
                property.name().equals("app.token")
                        && property.description().equals("Access token")
                        && property.required()
                        && property.sensitive());
    }

    @Test
    void fieldMetadataWinsOverGetterMetadataForSameProperty() throws IOException {
        Compilation compilation = compile("""
                package com.example;

                import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                import org.springframework.boot.context.properties.ConfigurationProperties;

                @ConfigurationProperties(prefix = "app")
                class DuplicateProperties {
                    @PropertyDoc(description = "Field description")
                    private String name;

                    @PropertyDoc(description = "Getter description")
                    public String getName() {
                        return name;
                    }
                }
                """);

        assertThat(compilation).succeeded();
        DocumentationBundle bundle = generatedBundle(compilation);
        assertThat(bundle.properties().stream()
                .filter(property -> property.name().equals("app.name"))
                .toList()).hasSize(1)
                .allMatch(property -> property.description().equals("Field description"));
    }

    @Test
    void readsSpringBootDeprecatedConfigurationPropertyFromGetter() throws IOException {
        Compilation compilation = compile("""
                package com.example;

                import org.springframework.boot.context.properties.ConfigurationProperties;
                import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

                @ConfigurationProperties(prefix = "app")
                class DeprecatedProperties {
                    private String oldName;

                    @DeprecatedConfigurationProperty(reason = "Renamed", replacement = "app.new-name")
                    public String getOldName() {
                        return oldName;
                    }
                }
                """);

        assertThat(compilation).succeeded();
        DocumentationBundle bundle = generatedBundle(compilation);
        assertThat(bundle.properties()).anyMatch(property ->
                property.name().equals("app.old-name")
                        && property.deprecation() != null
                        && property.deprecation().reason().equals("Renamed")
                        && property.deprecation().replacedBy().equals("app.new-name"));
    }

    @Test
    void warnsWhenPropertyAnnotationIsPlacedOnNonGetterMethod() {
        Compilation compilation = compile("""
                package com.example;

                import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                import org.springframework.boot.context.properties.ConfigurationProperties;

                @ConfigurationProperties(prefix = "app")
                class WarningProperties {
                    private String name;

                    @PropertyDoc(description = "Ignored")
                    public void helper() {
                    }
                }
                """);

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining("ignored on unsupported method");
    }

    @Test
    void doesNotWarnForRecordComponentAccessorMirrorAnnotations() throws IOException {
        Compilation compilation = compile("""
                package com.example;

                import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                import org.springframework.boot.context.properties.ConfigurationProperties;

                @ConfigurationProperties(prefix = "app")
                record RecordProperties(@PropertyDoc(description = "Name") String name) {
                }
                """);

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningCount(0);
        DocumentationBundle bundle = generatedBundle(compilation);
        assertThat(bundle.properties()).anyMatch(property ->
                property.name().equals("app.name") && property.description().equals("Name"));
    }

    @Test
    void accumulatesMetadataAcrossProcessingRounds() throws IOException {
        Compilation compilation = Compiler.javac()
                .withProcessors(new GeneratedPropertiesProcessor(), new SpringPropertiesMdProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.InitialProperties", """
                        package com.example;

                        import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                        import org.springframework.boot.context.properties.ConfigurationProperties;

                        @ConfigurationProperties(prefix = "initial")
                        class InitialProperties {
                            @PropertyDoc(description = "Initial name")
                            private String name;
                        }
                        """));

        assertThat(compilation).succeeded();
        DocumentationBundle bundle = generatedBundle(compilation);
        assertThat(bundle.properties()).anyMatch(property -> property.name().equals("initial.name"));
        assertThat(bundle.properties()).anyMatch(property -> property.name().equals("generated.name"));
    }

    private Compilation compile(String source) {
        return Compiler.javac()
                .withProcessors(new SpringPropertiesMdProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.TestProperties", source));
    }

    private DocumentationBundle generatedBundle(Compilation compilation) throws IOException {
        var generatedFile = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                MetadataJsonWriter.OUTPUT_PATH
        );
        assertThat(generatedFile).isPresent();
        return writer.fromJson(generatedFile.orElseThrow().getCharContent(false).toString());
    }

    @SupportedAnnotationTypes("*")
    private static final class GeneratedPropertiesProcessor extends AbstractProcessor {
        private boolean generated;

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latestSupported();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            if (generated || roundEnv.processingOver()) {
                return false;
            }
            generated = true;
            try {
                JavaFileObject source = processingEnv.getFiler()
                        .createSourceFile("com.example.GeneratedProperties");
                try (Writer writer = source.openWriter()) {
                    writer.write("""
                            package com.example;

                            import io.github.springpropertiesmd.api.annotation.PropertyDoc;
                            import org.springframework.boot.context.properties.ConfigurationProperties;

                            @ConfigurationProperties(prefix = "generated")
                            class GeneratedProperties {
                                @PropertyDoc(description = "Generated name")
                                private String name;
                            }
                            """);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return false;
        }
    }
}
