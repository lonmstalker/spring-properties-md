package io.github.springpropertiesmd.core.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanner {

    private static final String ENRICHED_METADATA_PATH = "META-INF/spring-properties-md/enriched-metadata.json";

    private final MetadataReader metadataReader;

    public ClasspathScanner() {
        this.metadataReader = new MetadataReader();
    }

    public List<Path> findMetadataFiles(List<Path> classpath) throws IOException {
        List<Path> found = new ArrayList<>();

        for (Path entry : classpath) {
            if (Files.isDirectory(entry)) {
                Path metadataFile = entry.resolve(ENRICHED_METADATA_PATH);
                if (Files.exists(metadataFile)) {
                    found.add(metadataFile);
                }
            } else if (entry.toString().endsWith(".jar") && Files.exists(entry)) {
                if (jarContainsMetadata(entry)) {
                    found.add(entry);
                }
            }
        }

        return found;
    }

    private boolean jarContainsMetadata(Path jarPath) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(ENRICHED_METADATA_PATH);
            return entry != null;
        }
    }
}
