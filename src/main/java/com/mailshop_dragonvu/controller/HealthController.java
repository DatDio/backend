package com.mailshop_dragonvu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight health endpoints for platform probes (e.g., Koyeb).
 * Keeps returning 200 even when security is enabled.
 */
@RestController
public class HealthController {

    @GetMapping({"/", "/health", "/api/health"})
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
