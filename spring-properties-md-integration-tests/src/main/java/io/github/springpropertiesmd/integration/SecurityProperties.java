package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.annotation.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
@PropertyGroupDoc(
        displayName = "Security Configuration",
        description = "Security and authentication settings",
        category = "Security",
        order = 3
)
public class SecurityProperties {

    @PropertyDoc(description = "JWT secret key", required = Requirement.REQUIRED, sensitive = true)
    @PropertySee("app.security.token-expiration")
    private String jwtSecret;

    @PropertyDoc(description = "Token expiration time in seconds")
    @PropertyExample(value = "3600", description = "1 hour")
    @PropertyExample(value = "86400", description = "1 day")
    @PropertySee("app.security.jwt-secret")
    private long tokenExpiration = 3600;

    @PropertyDoc(description = "Enable CORS support")
    private boolean corsEnabled = false;

    @PropertyDeprecation(
            reason = "Use app.security.jwt-secret instead",
            replacedBy = "app.security.jwt-secret",
            since = "1.0",
            removalVersion = "2.0"
    )
    @PropertyDoc(description = "Old API key (deprecated)")
    private String apiKey;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(long tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
