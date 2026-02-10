# Configuration Properties

## Table of Contents

- [Server Configuration](#server-configuration)
- [Database Configuration](#database-configuration)
- [Security Configuration](#security-configuration)


---

## Server Configuration

HTTP server settings


| Property | Type | Description | Default | Required | Constraints | Examples |
|----------|------|-------------|---------|----------|-------------|----------|
| `app.server.port` | `int` | Server port number |  | Yes |  | `8080` (default), `443` (HTTPS) |
| `app.server.host` | `String` | Server bind address |  | No |  | `0.0.0.0` (all interfaces) |
| `app.server.context-path` | `String` | Context path for the application |  | No |  |  |

## Database Configuration

Database connection settings


| Property | Type | Description | Default | Required | Constraints | Examples |
|----------|------|-------------|---------|----------|-------------|----------|
| `app.database.url` | `String` | JDBC connection URL |  | Yes |  | `jdbc:postgresql://localhost:5432/mydb` (PostgreSQL) |
| `app.database.username` | `String` | Database username |  | Yes |  |  |
| `app.database.password` | `String` | Database password |  | Yes |  |  |
| `app.database.max-pool-size` | `int` | Maximum connection pool size |  | No |  |  |

## Security Configuration

Security and authentication settings


| Property | Type | Description | Default | Required | Constraints | Examples |
|----------|------|-------------|---------|----------|-------------|----------|
| `app.security.jwt-secret` | `String` | JWT secret key |  | Yes |  |  |
| `app.security.token-expiration` | `long` | Token expiration time in seconds |  | No |  | `3600` (1 hour), `86400` (1 day) |
| `app.security.cors-enabled` | `boolean` | Enable CORS support |  | No |  |  |
| `app.security.api-key` | `String` | Old API key (deprecated) |  | No |  |  |
