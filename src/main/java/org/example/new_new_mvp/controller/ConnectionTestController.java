package org.example.new_new_mvp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// Temporarily disabled - requires DataSource which is not available with Supabase API approach
// @RestController
@RequestMapping("/api/connection-test")
public class ConnectionTestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            try (Connection connection = dataSource.getConnection()) {
                response.put("status", "success");
                response.put("message", "Supabase PostgreSQL connection successful!");
                response.put("database", connection.getMetaData().getDatabaseProductName());
                response.put("version", connection.getMetaData().getDatabaseProductVersion());
                response.put("url", connection.getMetaData().getURL());
                response.put("username", connection.getMetaData().getUserName());
                response.put("timestamp", java.time.LocalDateTime.now());
            }
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("sqlState", e.getSQLState());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connection test controller is working");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("status", "ready");
        return ResponseEntity.ok(response);
    }
}
