package io.github.springpropertiesmd.core.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocumentationFileWriter {

    public void write(RenderedDocumentation documentation) throws IOException {
        for (var entry : documentation.files().entrySet()) {
            Path parent = entry.getKey().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(entry.getKey(), entry.getValue());
        }
    }
}
