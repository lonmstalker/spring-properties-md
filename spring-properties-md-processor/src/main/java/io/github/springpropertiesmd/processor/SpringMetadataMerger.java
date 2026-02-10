package io.github.springpropertiesmd.processor;

import io.github.springpropertiesmd.api.model.DocumentationBundle;
import io.github.springpropertiesmd.api.model.GroupMetadata;
import io.github.springpropertiesmd.api.model.PropertyMetadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpringMetadataMerger {

    public DocumentationBundle merge(DocumentationBundle enriched, DocumentationBundle spring) {
        Map<String, GroupMetadata> groupMap = new LinkedHashMap<>();
        for (GroupMetadata g : spring.groups()) {
            groupMap.put(g.name(), g);
        }
        for (GroupMetadata g : enriched.groups()) {
            groupMap.put(g.name(), g);
        }

        Map<String, PropertyMetadata> propertyMap = new LinkedHashMap<>();
        for (PropertyMetadata p : spring.properties()) {
            propertyMap.put(p.name(), p);
        }
        for (PropertyMetadata p : enriched.properties()) {
            propertyMap.put(p.name(), mergeProperty(propertyMap.get(p.name()), p));
        }

        return new DocumentationBundle(
                new ArrayList<>(groupMap.values()),
                new ArrayList<>(propertyMap.values())
        );
    }

    private PropertyMetadata mergeProperty(PropertyMetadata spring, PropertyMetadata enriched) {
        if (spring == null) {
            return enriched;
        }
        return new PropertyMetadata(
                enriched.name(),
                firstNonEmpty(enriched.type(), spring.type()),
                firstNonEmpty(enriched.typeDisplay(), spring.typeDisplay()),
                firstNonEmpty(enriched.description(), spring.description()),
                firstNonEmpty(enriched.defaultValue(), spring.defaultValue()),
                enriched.required() || spring.required(),
                enriched.sensitive() || spring.sensitive(),
                enriched.profiles().isEmpty() ? spring.profiles() : enriched.profiles(),
                enriched.deprecation() != null ? enriched.deprecation() : spring.deprecation(),
                enriched.examples().isEmpty() ? spring.examples() : enriched.examples(),
                enriched.constraints().isEmpty() ? spring.constraints() : enriched.constraints(),
                firstNonEmpty(enriched.category(), spring.category()),
                firstNonEmpty(enriched.subcategory(), spring.subcategory()),
                firstNonEmpty(enriched.since(), spring.since()),
                enriched.seeAlso().isEmpty() ? spring.seeAlso() : enriched.seeAlso(),
                enriched.customMetadata().isEmpty() ? spring.customMetadata() : enriched.customMetadata(),
                firstNonEmpty(enriched.sourceType(), spring.sourceType()),
                firstNonEmpty(enriched.groupName(), spring.groupName())
        );
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.isEmpty()) return a;
        return b;
    }
}
