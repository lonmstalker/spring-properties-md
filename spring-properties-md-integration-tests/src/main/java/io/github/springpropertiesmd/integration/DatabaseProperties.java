package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.annotation.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.database")
@PropertyGroupDoc(
        displayName = "Database Configuration",
        description = "Database connection settings",
        category = "Database",
        order = 2
)
@PropertyCategory(value = "Infrastructure", subcategory = "Database")
public class DatabaseProperties {

    @PropertyDoc(description = "JDBC connection URL", required = Requirement.REQUIRED)
    @PropertyExample(value = "jdbc:postgresql://localhost:5432/mydb", description = "PostgreSQL")
    private String url;

    @PropertyDoc(description = "Database username", required = Requirement.REQUIRED)
    private String username;

    @PropertyDoc(description = "Database password", required = Requirement.REQUIRED, sensitive = true)
    @PropertyCustomMetadata(key = "env", value = "DB_PASSWORD")
    private String password;

    @PropertyDoc(description = "Maximum connection pool size")
    @PropertySince("1.1")
    private int maxPoolSize = 10;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
