package org.example.new_new_mvp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("supabaseUrl", supabaseUrl);
        response.put("mode", "Supabase API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "NEW_NEW_MVP");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("description", "Spring Boot application with Supabase API integration");
        response.put("supabaseUrl", supabaseUrl);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

