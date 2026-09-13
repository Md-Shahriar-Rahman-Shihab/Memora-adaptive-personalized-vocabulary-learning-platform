package com.memora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Main entry point for the Memora backend application.
 * Enables Spring Boot auto-configuration, component scanning, and JPA Auditing.
 */
@SpringBootApplication
@EnableJpaAuditing
public class MemoraApplication {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        loadDotEnv();
        SpringApplication.run(MemoraApplication.class, args);
    }

    /**
     * Safely loads key-value pairs from .env into System properties if not already set.
     * Never logs or outputs secret values.
     */
    private static void loadDotEnv() {
        File envFile = new File(".env");
        if (!envFile.exists() || !envFile.isFile()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    String val = line.substring(eqIndex + 1).trim();
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        if (val.length() >= 2) {
                            val = val.substring(1, val.length() - 1);
                        }
                    }
                    if ("DB_URL".equalsIgnoreCase(key) && val.matches("^jdbc:postgresql://[^/@]+:[^/@]+@.*")) {
                        val = val.replaceFirst("jdbc:postgresql://[^/@]+:[^/@]+@", "jdbc:postgresql://");
                    }
                    String envVal = System.getenv(key);
                    String propVal = System.getProperty(key);
                    if ((envVal == null || envVal.isBlank()) && (propVal == null || propVal.isBlank())) {
                        System.setProperty(key, val);
                    }
                }
            }
        } catch (IOException ignored) {
        }

        // Ensure active JDBC DB_URL is normalized without user:password@
        String effectiveDbUrl = System.getProperty("DB_URL");
        if (effectiveDbUrl == null || effectiveDbUrl.isBlank()) {
            effectiveDbUrl = System.getenv("DB_URL");
        }
        if (effectiveDbUrl != null && effectiveDbUrl.matches("^jdbc:postgresql://[^/@]+:[^/@]+@.*")) {
            System.setProperty("DB_URL", effectiveDbUrl.replaceFirst("jdbc:postgresql://[^/@]+:[^/@]+@", "jdbc:postgresql://"));
        }
    }
}

