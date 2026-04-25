package io.github.springpropertiesmd.core.generator;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record RenderedDocumentation(Map<Path, String> files) {

    public RenderedDocumentation {
        files = files == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(files));
    }

    public String singleContent() {
        if (files.size() != 1) {
            throw new IllegalStateException("Expected exactly one rendered file, got " + files.size());
        }
        return files.values().iterator().next();
    }

    public static RenderedDocumentation ordered(Map<Path, String> files) {
        return new RenderedDocumentation(new LinkedHashMap<>(files));
    }

    public static String normalize(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("\r\n", "\n").replace("\r", "\n").stripTrailing() + "\n";
    }
}
