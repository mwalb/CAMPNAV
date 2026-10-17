package com.campnav.backend.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            databaseUrl = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (databaseUrl == null || databaseUrl.isBlank()) {
            databaseUrl = properties.getUrl();
        }

        if (databaseUrl != null && !databaseUrl.isBlank() && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            try {
                // Fly Postgres provides DATABASE_URL in format postgres://user:pass@host:port/dbname
                String normalizedUrl = databaseUrl.replace("postgres://", "http://").replace("postgresql://", "http://");
                URI uri = new URI(normalizedUrl);
                
                String username = properties.getUsername();
                String password = properties.getPassword();
                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    username = URLDecoder.decode(userParts[0], StandardCharsets.UTF_8);
                    if (userParts.length > 1) {
                        password = URLDecoder.decode(userParts[1], StandardCharsets.UTF_8);
                    }
                }
                
                String query = (uri.getQuery() != null && !uri.getQuery().isBlank()) ? "?" + uri.getQuery() : "";
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = (uri.getPath() != null && !uri.getPath().isBlank()) ? uri.getPath() : "/campnav";
                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path + query;

                return DataSourceBuilder.create()
                        .driverClassName("org.postgresql.Driver")
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .build();
            } catch (Exception e) {
                // Fall back to standard spring.datasource.* configuration
            }
        }
        return properties.initializeDataSourceBuilder().build();
    }
}
