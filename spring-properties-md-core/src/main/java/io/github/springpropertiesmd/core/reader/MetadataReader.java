package io.github.springpropertiesmd.core.reader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetadataReader {

    private static final String METADATA_PATH = "META-INF/spring-properties-md/enriched-metadata.json";

    private final ObjectMapper objectMapper;

    public MetadataReader() {
        this.objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public DocumentationBundle read(Path file) throws IOException {
        return objectMapper.readValue(file.toFile(), DocumentationBundle.class);
    }

    public DocumentationBundle read(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, DocumentationBundle.class);
    }

    public DocumentationBundle readFromClassesDir(Path classesDir) throws IOException {
        Path metadataFile = classesDir.resolve(METADATA_PATH);
        if (Files.exists(metadataFile)) {
            return read(metadataFile);
        }
        return DocumentationBundle.empty();
    }

    public DocumentationBundle merge(List<DocumentationBundle> bundles) {
        List<GroupMetadata> allGroups = new ArrayList<>();
        List<PropertyMetadata> allProperties = new ArrayList<>();

        for (DocumentationBundle bundle : bundles) {
            allGroups.addAll(bundle.groups());
            allProperties.addAll(bundle.properties());
        }

        return new DocumentationBundle(allGroups, allProperties);
    }
}
