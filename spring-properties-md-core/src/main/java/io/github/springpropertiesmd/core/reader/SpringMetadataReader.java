package io.github.springpropertiesmd.core.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springpropertiesmd.api.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpringMetadataReader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentationBundle read(Path file) throws IOException {
        JsonNode root = objectMapper.readTree(file.toFile());
        return parse(root);
    }

    public DocumentationBundle read(InputStream inputStream) throws IOException {
        JsonNode root = objectMapper.readTree(inputStream);
        return parse(root);
    }

    private DocumentationBundle parse(JsonNode root) {
        List<GroupMetadata> groups = new ArrayList<>();
        List<PropertyMetadata> properties = new ArrayList<>();

        if (root.has("groups")) {
            for (JsonNode groupNode : root.get("groups")) {
                groups.add(new GroupMetadata(
                        textOrNull(groupNode, "name"),
                        textOrNull(groupNode, "name"),
                        textOrNull(groupNode, "description"),
                        textOrNull(groupNode, "sourceType"),
                        "",
                        Integer.MAX_VALUE
                ));
            }
        }

        if (root.has("properties")) {
            for (JsonNode propNode : root.get("properties")) {
                DeprecationInfo deprecation = null;
                if (propNode.has("deprecation")) {
                    JsonNode depNode = propNode.get("deprecation");
                    deprecation = new DeprecationInfo(
                            textOrNull(depNode, "reason"),
                            textOrNull(depNode, "replacement"),
                            null, null
                    );
                }

                properties.add(new PropertyMetadata(
                        textOrNull(propNode, "name"),
                        textOrNull(propNode, "type"),
                        null,
                        textOrNull(propNode, "description"),
                        textOrNull(propNode, "defaultValue"),
                        false, false,
                        null, deprecation, null, null,
                        null, null, null, null, null,
                        textOrNull(propNode, "sourceType"),
                        null
                ));
            }
        }

        return new DocumentationBundle(groups, properties);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child != null && !child.isNull() ? child.asText() : null;
    }
}
