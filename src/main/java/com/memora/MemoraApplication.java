package com.memora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Memora backend application.
 * Enables Spring Boot auto-configuration, component scanning, and JPA Auditing.
 */
@SpringBootApplication
@EnableJpaAuditing
public class MemoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoraApplication.class, args);
    }
}
