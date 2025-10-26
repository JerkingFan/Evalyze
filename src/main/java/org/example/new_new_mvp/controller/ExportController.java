package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {
    
    @Autowired
    private ExportService exportService;
    
    @GetMapping("/all-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> exportAllData() {
        try {
            System.out.println("Exporting all database data...");
            Map<String, Object> allData = exportService.exportAllData();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "evalyze_export_" + timestamp + ".json";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(allData);
                    
        } catch (Exception e) {
            System.out.println("Error exporting data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> exportCompanies() {
        try {
            Map<String, Object> companiesData = exportService.exportCompanies();
            return ResponseEntity.ok(companiesData);
        } catch (Exception e) {
            System.out.println("Error exporting companies: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> exportProfiles() {
        try {
            Map<String, Object> profilesData = exportService.exportProfiles();
            return ResponseEntity.ok(profilesData);
        } catch (Exception e) {
            System.out.println("Error exporting profiles: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> exportUsers() {
        try {
            Map<String, Object> usersData = exportService.exportUsers();
            return ResponseEntity.ok(usersData);
        } catch (Exception e) {
            System.out.println("Error exporting users: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/sql")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportSQL() {
        try {
            System.out.println("Exporting SQL queries...");
            String sqlQueries = exportService.exportSQL();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "evalyze_sql_export_" + timestamp + ".sql";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(sqlQueries);
                    
        } catch (Exception e) {
            System.out.println("Error exporting SQL: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
