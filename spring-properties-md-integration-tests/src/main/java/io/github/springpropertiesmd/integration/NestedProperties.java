package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.annotation.PropertyDoc;
import io.github.springpropertiesmd.api.annotation.PropertyGroupDoc;
import io.github.springpropertiesmd.api.annotation.Requirement;
import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.nested")
@PropertyGroupDoc(
        displayName = "Nested Configuration",
        description = "Nested property expansion test",
        order = 4
)
public record NestedProperties(
        @PropertyDoc(description = "Simple name") String name,
        @PropertyDoc(description = "Database settings") @Valid Database database
) {
    public record Database(
            @PropertyDoc(description = "DB host", required = Requirement.REQUIRED) String host,
            @PropertyDoc(description = "DB port") int port,
            @PropertyDoc(description = "Connection pool") @Valid Pool pool
    ) {
        public record Pool(
                @PropertyDoc(description = "Pool size") int size,
                @PropertyDoc(description = "Pool timeout") long timeout
        ) {}
    }
}