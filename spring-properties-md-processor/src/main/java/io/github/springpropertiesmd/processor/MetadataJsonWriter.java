package io.github.springpropertiesmd.processor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.springpropertiesmd.api.model.DocumentationBundle;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

public class MetadataJsonWriter {

    public static final String OUTPUT_PATH = "META-INF/spring-properties-md/enriched-metadata.json";

    private final ObjectMapper objectMapper;

    public MetadataJsonWriter() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void write(DocumentationBundle bundle, Filer filer) throws IOException {
        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_PATH);
        try (Writer writer = resource.openWriter()) {
            writer.write(toJson(bundle));
        }
    }

    public String toJson(DocumentationBundle bundle) throws IOException {
        return objectMapper.writeValueAsString(bundle);
    }

    public DocumentationBundle fromJson(String json) throws IOException {
        return objectMapper.readValue(json, DocumentationBundle.class);
    }

    public void write(DocumentationBundle bundle, Path outputFile) throws IOException {
        objectMapper.writeValue(outputFile.toFile(), bundle);
    }

    public DocumentationBundle read(Path inputFile) throws IOException {
        return objectMapper.readValue(inputFile.toFile(), DocumentationBundle.class);
    }
}
