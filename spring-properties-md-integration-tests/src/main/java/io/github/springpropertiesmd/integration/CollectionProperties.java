package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.annotation.PropertyDoc;
import io.github.springpropertiesmd.api.annotation.PropertyGroupDoc;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.collection")
@PropertyGroupDoc(
        displayName = "Collection Configuration",
        description = "Collection, map and enum property tests",
        order = 5
)
public record CollectionProperties(
        @PropertyDoc(description = "Service endpoints") java.util.List<Endpoint> endpoints,
        @PropertyDoc(description = "Tags list") java.util.List<String> tags,
        @PropertyDoc(description = "Data sources") java.util.Map<String, DataSource> dataSources,
        @PropertyDoc(description = "Settings map") java.util.Map<String, String> settings,
        @PropertyDoc(description = "Log level") LogLevel logLevel,
        @PropertyDoc(description = "Active log levels") java.util.List<LogLevel> activeLogLevels,
        @PropertyDoc(description = "Level overrides") java.util.Map<String, LogLevel> levelOverrides
) {
    public record Endpoint(
            @PropertyDoc(description = "Endpoint URL") String url,
            @PropertyDoc(description = "Timeout in ms") int timeout
    ) {}

    public record DataSource(
            @PropertyDoc(description = "JDBC URL") String url,
            @PropertyDoc(description = "DB username") String username,
            @PropertyDoc(description = "Connection pool") Pool pool
    ) {
        public record Pool(
                @PropertyDoc(description = "Max pool size") int maxSize
        ) {}
    }

    public enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }
}
