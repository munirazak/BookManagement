package com.kopibru.bookmanagement.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Ensures the database from {@code spring.datasource.url} exists (local/Docker TCP only),
 * then runs table DDL from {@code schema.sql}.
 * <p>
 * Cloud Run → Cloud SQL uses the Java Connector ({@code cloudSqlInstance}). In that case
 * {@code CREATE DATABASE} is skipped — create the database in Cloud SQL Console first.
 */
public class SchemaBootstrap implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Pattern SAFE_DB_NAME = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        if (!env.getProperty("app.schema-bootstrap.enabled", Boolean.class, true)) {
            return;
        }

        String jdbcUrl = env.getRequiredProperty("spring.datasource.url");
        String username = env.getRequiredProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password", "");

        validateCloudRunDatasource(env, jdbcUrl);

        String databaseName = extractDatabaseName(jdbcUrl);
        if (!SAFE_DB_NAME.matcher(databaseName).matches()) {
            throw new IllegalStateException("Unsafe database name in datasource URL: " + databaseName);
        }

        boolean cloudSql = isCloudSqlConnectorUrl(jdbcUrl);

        try {
            if (!cloudSql) {
                String adminUrl = buildAdminUrl(jdbcUrl);
                ensureDatabaseExists(adminUrl, username, password, databaseName);
            }
            executeSchemaScript(jdbcUrl, username, password);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize database schema", ex);
        }
    }

    /**
     * Fail fast on Cloud Run when prod still points at localhost (missing CLOUD_SQL_INSTANCE).
     */
    private void validateCloudRunDatasource(Environment env, String jdbcUrl) {
        boolean prod = false;
        for (String profile : env.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                prod = true;
                break;
            }
        }
        if (!prod) {
            return;
        }

        String lower = jdbcUrl.toLowerCase();
        if (lower.contains("localhost") || lower.contains("127.0.0.1")) {
            throw new IllegalStateException(
                    "Prod datasource points at localhost. On Cloud Run set CLOUD_SQL_INSTANCE="
                            + "project:region:instance (and do not set DB_URL to localhost). "
                            + "Example JDBC default: jdbc:postgresql:///book_catalogue"
                            + "?cloudSqlInstance=PROJECT:REGION:INSTANCE"
                            + "&socketFactory=com.google.cloud.sql.postgres.SocketFactory");
        }

        if (isCloudSqlConnectorUrl(jdbcUrl)) {
            String instance = env.getProperty("CLOUD_SQL_INSTANCE", "");
            if (!StringUtils.hasText(instance) && !jdbcUrl.matches("(?i).*cloudSqlInstance=[^&]+.*")) {
                throw new IllegalStateException(
                        "CLOUD_SQL_INSTANCE is required for Cloud SQL Java Connector "
                                + "(format: project:region:instance).");
            }
            // Empty cloudSqlInstance= in URL
            if (lower.contains("cloudsqlinstance=&") || lower.endsWith("cloudsqlinstance=")) {
                throw new IllegalStateException(
                        "CLOUD_SQL_INSTANCE is empty. Set it to your Cloud SQL instance "
                                + "connection name (project:region:instance).");
            }
        }
    }

    private boolean isCloudSqlConnectorUrl(String jdbcUrl) {
        String lower = jdbcUrl.toLowerCase();
        return lower.contains("cloudsqlinstance=")
                || lower.contains("socketfactory=com.google.cloud.sql");
    }

    private String buildAdminUrl(String jdbcUrl) {
        String[] parts = jdbcUrl.split("\\?", 2);
        String base = parts[0];
        String query = parts.length > 1 ? "?" + parts[1] : "";
        int lastSlash = base.lastIndexOf('/');
        return base.substring(0, lastSlash + 1) + "postgres" + query;
    }

    private void ensureDatabaseExists(
            String adminUrl, String username, String password, String databaseName) throws Exception {
        try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
             Statement stmt = conn.createStatement()) {
            if (!databaseExists(stmt, databaseName)) {
                stmt.execute("CREATE DATABASE " + databaseName);
            }
        }
    }

    private void executeSchemaScript(String jdbcUrl, String username, String password) throws Exception {
        List<String> statements = parseStatements(loadSchemaSql());
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
        }
    }

    private boolean databaseExists(Statement stmt, String databaseName) throws Exception {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'")) {
            return rs.next();
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        String withoutParams = jdbcUrl.split("\\?", 2)[0];
        int lastSlash = withoutParams.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == withoutParams.length() - 1) {
            throw new IllegalStateException("Cannot parse database name from JDBC URL: " + jdbcUrl);
        }
        return withoutParams.substring(lastSlash + 1);
    }

    private String loadSchemaSql() throws Exception {
        return StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    private List<String> parseStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(trimmed).append(' ');
            if (trimmed.endsWith(";")) {
                String sql = current.toString().trim();
                statements.add(sql.substring(0, sql.length() - 1).trim());
                current.setLength(0);
            }
        }
        return statements;
    }
}
