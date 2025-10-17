package org.example.new_new_mvp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getAppInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "IT-Профиль AI");
        info.put("version", "1.0.0");
        info.put("description", "Платформа для создания AI-профилей сотрудников");
        return ResponseEntity.ok(info);
    }
}
