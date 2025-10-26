package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.dto.SupabaseUserDto;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupabaseUserRepository {

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    @Qualifier("supabaseWebClient")
    private WebClient supabaseWebClient;
    
    /**
     * Конвертирует SupabaseUserDto в User модель
     * Парсит status поле, чтобы извлечь role и company_id
     */
    private User convertDtoToUser(SupabaseUserDto dto) {
        System.out.println("=== convertDtoToUser START ===");
        System.out.println("DTO: id=" + dto.getId() + ", email=" + dto.getEmail() + ", status=" + dto.getStatus());
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(dto.getPassword());
        user.setTelegramChatId(dto.getTelegramChatId());
        user.setActivationCode(dto.getActivationCode());
        user.setCreatedAt(dto.getCreatedAt());
        user.setLastUpdated(dto.getLastUpdated());
        
        // status - это текстовое поле ('invited', 'active', 'company', 'employee', и т.д.)
        // Используем его для определения роли пользователя
        user.setStatus(dto.getStatus());
        
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            // Определяем роль на основе статуса
            if (dto.getStatus().equalsIgnoreCase("company") || dto.getStatus().equalsIgnoreCase("active")) {
                user.setRole(UserRole.COMPANY);
            } else {
                user.setRole(UserRole.EMPLOYEE);
            }
            System.out.println("Set role based on status '" + dto.getStatus() + "': " + user.getRole());
        } else {
            System.out.println("WARNING: status is null or empty, defaulting to EMPLOYEE");
            user.setRole(UserRole.EMPLOYEE);
        }
        
        // company_id нужно получать отдельно, если требуется
        // Пока оставляем null, так как в схеме users нет прямого поля company_id
        
        // Устанавливаем id как UUID (конвертируем из Long)
        if (dto.getId() != null) {
            // Для совместимости создаем UUID из Long id
            UUID userId = UUID.nameUUIDFromBytes(String.valueOf(dto.getId()).getBytes());
            System.out.println("Converted ID: " + dto.getId() + " -> " + userId);
            user.setId(userId);
        }
        
        System.out.println("=== convertDtoToUser END ===");
        return user;
    }

    /**
     * Найти пользователя по email
     */
    public Mono<Optional<User>> findByEmail(String email) {
        Map<String, String> filters = Map.of("email", "eq." + email);
        // Используем SupabaseUserDto, затем конвертируем в User
        return supabaseService.select("users", SupabaseUserDto.class, filters)
                .map(dtos -> dtos.isEmpty() ? Optional.empty() : Optional.of(convertDtoToUser(dtos.get(0))));
    }

    /**
     * Проверить существование пользователя по email
     */
    public Mono<Boolean> existsByEmail(String email) {
        System.out.println("SupabaseUserRepository.existsByEmail(" + email + ")");
        Map<String, String> filters = Map.of("email", "eq." + email);
        // Используем SupabaseUserDto вместо User, чтобы избежать ошибки парсинга UUID
        return supabaseService.select("users", SupabaseUserDto.class, filters)
                .map(dtos -> {
                    boolean exists = !dtos.isEmpty();
                    System.out.println("User exists: " + exists);
                    return exists;
                })
                .doOnError(error -> System.out.println("Error checking existsByEmail: " + error.getMessage()));
    }
    
    /**
     * Найти пользователя по activation_code
     */
    public Mono<Optional<User>> findByActivationCode(String activationCode) {
        System.out.println("SupabaseUserRepository.findByActivationCode(" + activationCode + ")");
        Map<String, String> filters = Map.of("activation_code", "eq." + activationCode);
        return supabaseService.select("users", SupabaseUserDto.class, filters)
                .<Optional<User>>map(dtos -> {
                    if (dtos.isEmpty()) {
                        System.out.println("No user found with activation_code: " + activationCode);
                        return Optional.empty();
                    }
                    System.out.println("User found with activation_code: " + activationCode);
                    return Optional.of(convertDtoToUser(dtos.get(0)));
                })
                .doOnError(error -> System.out.println("Error finding by activation_code: " + error.getMessage()));
    }
    
    /**
     * Проверить существование пользователя по activation_code
     */
    public Mono<Boolean> existsByActivationCode(String activationCode) {
        System.out.println("SupabaseUserRepository.existsByActivationCode(" + activationCode + ")");
        Map<String, String> filters = Map.of("activation_code", "eq." + activationCode);
        return supabaseService.select("users", SupabaseUserDto.class, filters)
                .map(dtos -> {
                    boolean exists = !dtos.isEmpty();
                    System.out.println("User with activation_code exists: " + exists);
                    return exists;
                })
                .doOnError(error -> System.out.println("Error checking existsByActivationCode: " + error.getMessage()));
    }

    /**
     * Найти пользователей по роли
     */
    public Mono<List<User>> findByRole(UserRole role) {
        // role хранится в status JSON, нужно получить всех и фильтровать локально
        return supabaseService.select("users", SupabaseUserDto.class, Map.of())
                .map(dtos -> dtos.stream()
                        .map(this::convertDtoToUser)
                        .filter(user -> user.getRole() == role)
                        .toList());
    }

    /**
     * Найти пользователей по ID компании
     */
    public Mono<List<User>> findByCompanyId(UUID companyId) {
        // company_id хранится в status JSON, нужно получить всех и фильтровать локально
        return supabaseService.select("users", SupabaseUserDto.class, Map.of())
                .map(dtos -> dtos.stream()
                        .map(this::convertDtoToUser)
                        .filter(user -> user.getCompany() != null && user.getCompany().getId().equals(companyId))
                        .toList());
    }

    /**
     * Найти пользователей по ID компании и роли
     */
    public Mono<List<User>> findByCompanyIdAndRole(UUID companyId, UserRole role) {
        // Получаем всех и фильтруем локально
        return supabaseService.select("users", SupabaseUserDto.class, Map.of())
                .map(dtos -> dtos.stream()
                        .map(this::convertDtoToUser)
                        .filter(user -> user.getRole() == role && 
                                       user.getCompany() != null && 
                                       user.getCompany().getId().equals(companyId))
                        .toList());
    }

    /**
     * Подсчитать количество пользователей в компании
     */
    public Mono<Long> countByCompanyId(UUID companyId) {
        return findByCompanyId(companyId).map(list -> (long) list.size());
    }

    /**
     * Подсчитать количество пользователей по роли
     */
    public Mono<Long> countByRole(UserRole role) {
        return findByRole(role).map(list -> (long) list.size());
    }

    /**
     * Сохранить пользователя (INSERT или UPDATE)
     * Адаптировано под существующую структуру таблицы users
     */
    public Mono<User> save(User user) {
        // Проверяем, существует ли пользователь по email
        return existsByEmail(user.getEmail())
            .flatMap(exists -> {
                if (exists) {
                    System.out.println("User exists, updating: " + user.getEmail());
                    return updateUser(user);
                } else {
                    System.out.println("User does not exist, inserting: " + user.getEmail());
                    return insertUser(user);
                }
            });
    }
    
    /**
     * Вставить нового пользователя
     */
    private Mono<User> insertUser(User user) {
        // Convert User to Map for Supabase API
        // Используем ТОЛЬКО существующие колонки!
        Map<String, Object> userMap = new java.util.HashMap<>();
        
        // НЕ отправляем id - он bigint auto-increment
        userMap.put("email", user.getEmail());
        userMap.put("full_name", user.getFullName());
        
        // activation_code - обязательное поле! Используем из объекта или генерируем
        String activationCode = user.getActivationCode();
        if (activationCode == null || activationCode.isEmpty()) {
            activationCode = UUID.randomUUID().toString();
        }
        userMap.put("activation_code", activationCode);
        
        // Добавляем остальные поля
        if (user.getCreatedAt() != null) {
            // Конвертируем OffsetDateTime в ISO-8601 строку
            userMap.put("created_at", user.getCreatedAt().toString());
        }
        
        // status - это просто текстовое поле ('invited', 'active', и т.д.)
        if (user.getStatus() != null && !user.getStatus().isEmpty()) {
            userMap.put("status", user.getStatus());
        } else {
            // Используем значение по умолчанию
            userMap.put("status", "invited");
        }
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            userMap.put("password", user.getPassword());
        }
        
        if (user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty()) {
            userMap.put("telegram_chat_id", user.getTelegramChatId());
        }
        
        // Инициализируем все boolean поля (если они NOT NULL с default false)
        // Если в БД есть DEFAULT, можно не отправлять, но для надежности инициализируем
        // userMap.put("Текст извечен", false);
        // userMap.put("Изображения извлечены", false);
        // userMap.put("Таблицы извлечены", false)
        
        // OAuth token removed - using email auth instead
        
        // PostgREST expects an array of objects, not a single object
        java.util.List<Map<String, Object>> userArray = java.util.Collections.singletonList(userMap);
        
        System.out.println("=== SUPABASE INSERT DEBUG ===");
        System.out.println("Inserting user into Supabase: " + userMap);
        System.out.println("Request body (as array): " + userArray);
        
        // Use raw insert with WebClient directly
        // PostgREST returns an array, so we need to extract the first element
        return supabaseWebClient.post()
                .uri("/users")
                .header("Prefer", "return=representation") // Tell PostgREST to return the inserted row
                .bodyValue(userArray) // Send as array
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            System.out.println("=== SUPABASE ERROR RESPONSE ===");
                            System.out.println("Status: " + response.statusCode());
                            System.out.println("Headers: " + response.headers().asHttpHeaders());
                            System.out.println("Body: " + errorBody);
                            System.out.println("================================");
                            return new RuntimeException("Supabase error: " + errorBody);
                        })
                )
                .bodyToFlux(SupabaseUserDto.class) // Response is an array of SupabaseUserDto
                .doOnNext(dto -> System.out.println("Received SupabaseUserDto: id=" + dto.getId() + ", email=" + dto.getEmail()))
                .next() // Get first element
                .map(dto -> {
                    System.out.println("Converting SupabaseUserDto to User...");
                    User convertedUser = convertDtoToUser(dto);
                    System.out.println("Conversion successful");
                    return convertedUser;
                }) // Convert to User model
                .doOnNext(savedUser -> {
                    System.out.println("=== SUCCESS ===");
                    System.out.println("User saved successfully: " + savedUser.getEmail());
                    System.out.println("User ID: " + savedUser.getId());
                    System.out.println("User Role: " + savedUser.getRole());
                    System.out.println("===============");
                })
                .doOnError(error -> {
                    System.out.println("=== ERROR ===");
                    System.out.println("Error type: " + error.getClass().getName());
                    System.out.println("Error message: " + error.getMessage());
                    System.out.println("Full stack trace:");
                    error.printStackTrace();
                    System.out.println("=============");
                });
    }

    /**
     * Обновить существующего пользователя
     */
    private Mono<User> updateUser(User user) {
        System.out.println("=== UPDATING USER ===");
        System.out.println("Email: " + user.getEmail());
        
        // Формируем данные для обновления
        Map<String, Object> updateMap = new java.util.HashMap<>();
        
        if (user.getFullName() != null) {
            updateMap.put("full_name", user.getFullName());
        }
        
        // status - это просто текстовое поле ('invited', 'active', и т.д.)
        if (user.getStatus() != null && !user.getStatus().isEmpty()) {
            updateMap.put("status", user.getStatus());
        }
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            updateMap.put("password", user.getPassword());
        }
        if (user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty()) {
            updateMap.put("telegram_chat_id", user.getTelegramChatId());
        }
        if (user.getLastUpdated() != null) {
            // Конвертируем OffsetDateTime в ISO-8601 строку
            updateMap.put("last_updated", user.getLastUpdated().toString());
        }
        
        System.out.println("Update data: " + updateMap);
        
        // Обновляем по email
        Map<String, String> filters = Map.of("email", "eq." + user.getEmail());
        
        return supabaseWebClient.patch()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users");
                    filters.forEach((key, value) -> uriBuilder.queryParam(key, value));
                    return uriBuilder.build();
                })
                .header("Prefer", "return=representation")
                .bodyValue(updateMap)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            System.out.println("=== UPDATE ERROR ===");
                            System.out.println("Status: " + response.statusCode());
                            System.out.println("Error: " + errorBody);
                            return new RuntimeException("Supabase update error: " + errorBody);
                        })
                )
                .bodyToFlux(SupabaseUserDto.class)
                .next()
                .map(this::convertDtoToUser)
                .doOnNext(updatedUser -> {
                    System.out.println("User updated successfully: " + updatedUser.getEmail());
                    System.out.println("====================");
                })
                .doOnError(error -> {
                    System.out.println("=== UPDATE ERROR ===");
                    System.out.println("Error: " + error.getMessage());
                    error.printStackTrace();
                    System.out.println("====================");
                });
    }
    
    /**
     * Обновить поле "Role" по email
     */
    public Mono<Boolean> updateUserRoleByEmail(String email, UUID roleId) {
        System.out.println("Updating users.\"Role\" for email=" + email + " to roleId=" + roleId);
        Map<String, String> filters = Map.of("email", "eq." + email);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("Role", roleId != null ? roleId.toString() : null);
        
        return supabaseWebClient.patch()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users");
                    filters.forEach((k, v) -> uriBuilder.queryParam(k, v));
                    return uriBuilder.build();
                })
                .header("Prefer", "return=representation")
                .bodyValue(body)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(errorBody -> new RuntimeException("Supabase update Role error: " + errorBody))
                )
                .bodyToFlux(SupabaseUserDto.class)
                .hasElements()
                .map(updated -> {
                    System.out.println("Role update result: " + updated);
                    return updated;
                });
    }
    
    /**
     * Обновить поле "Role" по activation_code
     */
    public Mono<Boolean> updateUserRoleByActivationCode(String activationCode, UUID roleId) {
        System.out.println("Updating users.\"Role\" for activation_code=" + activationCode + " to roleId=" + roleId);
        Map<String, String> filters = Map.of("activation_code", "eq." + activationCode);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("Role", roleId != null ? roleId.toString() : null);
        
        return supabaseWebClient.patch()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users");
                    filters.forEach((k, v) -> uriBuilder.queryParam(k, v));
                    return uriBuilder.build();
                })
                .header("Prefer", "return=representation")
                .bodyValue(body)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(errorBody -> new RuntimeException("Supabase update Role error: " + errorBody))
                )
                .bodyToFlux(SupabaseUserDto.class)
                .hasElements()
                .map(updated -> {
                    System.out.println("Role update (by activation_code) result: " + updated);
                    return updated;
                });
    }
    
    /**
     * Найти пользователя по ID
     */
    public Mono<Optional<User>> findById(UUID id) {
        // В Supabase id это bigint, нужно искать по email или получить всех
        // Пока возвращаем пустой Optional как заглушку
        return Mono.just(Optional.empty());
    }

    /**
     * Удалить пользователя
     */
    public Mono<Void> deleteById(UUID id) {
        // Аналогично, ID в Supabase это bigint, а не UUID
        return Mono.empty();
    }

    /**
     * Получить всех пользователей
     */
    public Mono<List<User>> findAll() {
        return supabaseService.select("users", SupabaseUserDto.class)
                .map(dtos -> dtos.stream()
                        .map(this::convertDtoToUser)
                        .toList());
    }

    /**
     * Подсчитать всех пользователей
     */
    public Mono<Long> count() {
        return findAll().map(list -> (long) list.size());
    }
}
