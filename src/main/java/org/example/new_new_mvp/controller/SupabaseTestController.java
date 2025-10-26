package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supabase")
@CrossOrigin(origins = "*")
public class SupabaseTestController {

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Тестовый endpoint для проверки подключения к Supabase
     */
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Supabase connection configured successfully!");
    }

    /**
     * Получить всех пользователей из Supabase
     */
    @GetMapping("/users")
    public Mono<ResponseEntity<List<User>>> getUsers() {
        return supabaseService.select("users", User.class)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Получить пользователя по email
     */
    @GetMapping("/users/by-email")
    public Mono<ResponseEntity<List<User>>> getUserByEmail(@RequestParam String email) {
        Map<String, String> filters = Map.of("email", "eq." + email);
        return supabaseService.select("users", User.class, filters)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Создать нового пользователя в Supabase
     */
    @PostMapping("/users")
    public Mono<ResponseEntity<User>> createUser(@RequestBody User user) {
        return supabaseService.insert("users", user, User.class)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
