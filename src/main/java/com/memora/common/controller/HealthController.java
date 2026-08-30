package com.memora.common.controller;

import com.memora.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check controller providing simple liveness and diagnostic status for Memora.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> healthInfo = new LinkedHashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "Memora Backend Platform");
        healthInfo.put("version", "1.0.0");
        healthInfo.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(ApiResponse.success("Memora backend is running normally", healthInfo));
    }
}
