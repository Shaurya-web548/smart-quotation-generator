package com.smartquote.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Normalizes the datasource URL BEFORE Flyway / DataSource auto-configuration runs.
 *
 * Accepts any of these in SPRING_DATASOURCE_URL (or spring.datasource.url):
 *   jdbc:postgresql://host:5432/db?sslmode=require   (used as-is)
 *   postgresql://user:pass@host:5432/db              (Render connectionString)
 *   postgres://user:pass@host:5432/db                (same handling)
 *
 * The PostgreSQL JDBC driver only accepts the jdbc:postgresql:// form, so the
 * native postgres:// form is converted and the embedded userinfo is split out
 * into spring.datasource.username / password when those are not already set.
 */
public class DatabaseUrlNormalizer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        String url = firstNonBlank(
                env.getProperty("SPRING_DATASOURCE_URL"),
                env.getProperty("spring.datasource.url"));
        if (url == null || url.isBlank()) {
            return;
        }
        String trimmed = url.trim();
        if (trimmed.regionMatches(true, 0, "jdbc:postgresql://", 0, "jdbc:postgresql://".length())) {
            return; // already JDBC, nothing to do
        }
        String lower = trimmed.toLowerCase();
        if (!(lower.startsWith("postgresql://") || lower.startsWith("postgres://"))) {
            return; // not a postgres URL at all (e.g. H2 in dev) - leave alone
        }
        try {
            URI uri = new URI(trimmed);
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath() != null && !uri.getPath().isBlank() ? uri.getPath() : "/smartquote";
            String query = uri.getQuery();
            StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                    .append(host).append(':').append(port).append(path);
            if (query != null && !query.isBlank()) {
                jdbc.append('?').append(query);
            } else {
                jdbc.append("?sslmode=require");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("spring.datasource.url", jdbc.toString());
            map.put("SPRING_DATASOURCE_URL", jdbc.toString());
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] up = userInfo.split(":", 2);
                if (firstNonBlank(env.getProperty("SPRING_DATASOURCE_USERNAME"),
                        env.getProperty("spring.datasource.username")) == null) {
                    map.put("spring.datasource.username", up[0]);
                    map.put("SPRING_DATASOURCE_USERNAME", up[0]);
                }
                if (up.length > 1 && firstNonBlank(env.getProperty("SPRING_DATASOURCE_PASSWORD"),
                        env.getProperty("spring.datasource.password")) == null) {
                    map.put("spring.datasource.password", up[1]);
                    map.put("SPRING_DATASOURCE_PASSWORD", up[1]);
                }
            }
            String existing = null;
            for (PropertySource<?> ps : env.getPropertySources()) {
                if ("databaseUrlNormalizer".equals(ps.getName()) && ps instanceof MapPropertySource mps) {
                    existing = "found";
                    mps.getSource().putAll(map);
                    break;
                }
            }
            if (existing == null) {
                env.getPropertySources().addFirst(new MapPropertySource("databaseUrlNormalizer", map));
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Invalid SPRING_DATASOURCE_URL - expected jdbc:postgresql://... or postgresql://... but got: "
                            + mask(trimmed),
                    e);
        }
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static String mask(String url) {
        int at = url.indexOf('@');
        if (at > 0) {
            int scheme = url.indexOf("://");
            return (scheme > 0 ? url.substring(0, scheme + 3) : "") + "***" + url.substring(at);
        }
        return url;
    }
}
