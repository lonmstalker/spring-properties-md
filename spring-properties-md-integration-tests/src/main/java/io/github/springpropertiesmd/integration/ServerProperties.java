package io.github.springpropertiesmd.integration;

import io.github.springpropertiesmd.api.annotation.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.server")
@PropertyGroupDoc(
        displayName = "Server Configuration",
        description = "HTTP server settings",
        category = "Server",
        order = 1
)
public class ServerProperties {

    @PropertyDoc(description = "Server port number", required = Requirement.REQUIRED)
    @PropertyExample(value = "8080", description = "default")
    @PropertyExample(value = "443", description = "HTTPS")
    @PropertySince("1.0")
    private int port = 8080;

    @PropertyDoc(description = "Server bind address", required = Requirement.OPTIONAL)
    @PropertyExample(value = "0.0.0.0", description = "all interfaces")
    private String host = "localhost";

    @PropertyDoc(description = "Context path for the application")
    private String contextPath = "/";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
