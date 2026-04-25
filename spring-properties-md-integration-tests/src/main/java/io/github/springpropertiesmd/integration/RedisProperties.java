package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.annotation.PropertyDoc;
import io.github.springpropertiesmd.api.annotation.PropertyExample;
import io.github.springpropertiesmd.api.annotation.PropertyGroupDoc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis")
@PropertyGroupDoc(
        displayName = "Redis Configuration",
        description = "Redis integration settings",
        category = "Cache",
        order = 6
)
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public record RedisProperties(
        @PropertyDoc(description = "Enables Redis integration")
        boolean enabled,

        @PropertyDoc(description = "Redis server host")
        @PropertyExample(value = "localhost", description = "local development")
        String host,

        @PropertyDoc(description = "Redis server port")
        @PropertyExample(value = "6379", description = "default")
        int port
) {
}
