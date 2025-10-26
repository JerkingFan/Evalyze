package org.example.new_new_mvp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class SupabaseService {

    @Autowired
    private WebClient supabaseWebClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Выполнить SELECT запрос к таблице
     */
    public <T> Mono<List<T>> select(String table, Class<T> clazz) {
        return supabaseWebClient.get()
                .uri("/" + table)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing JSON response", e);
                    }
                });
    }

    /**
     * Выполнить SELECT запрос с фильтрацией
     */
    public <T> Mono<List<T>> select(String table, Class<T> clazz, Map<String, String> filters) {
        return supabaseWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/" + table);
                    // Добавляем фильтры как query параметры
                    for (Map.Entry<String, String> filter : filters.entrySet()) {
                        builder.queryParam(filter.getKey(), filter.getValue());
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing JSON response", e);
                    }
                });
    }

    /**
     * Выполнить INSERT запрос
     * Note: PostgREST expects an array of objects for INSERT
     */
    public <T> Mono<T> insert(String table, T data, Class<T> clazz) {
        System.out.println("=== SUPABASE INSERT ===");
        System.out.println("Table: " + table);
        System.out.println("Data: " + data);
        System.out.println("Data class: " + data.getClass().getName());
        
        // Wrap data in array for PostgREST
        java.util.List<T> dataArray = java.util.Collections.singletonList(data);
        
        // Serialize to JSON to see exact payload
        try {
            String jsonPayload = objectMapper.writeValueAsString(dataArray);
            System.out.println("JSON Payload: " + jsonPayload);
        } catch (Exception e) {
            System.out.println("Error serializing payload: " + e.getMessage());
        }
        System.out.println("=======================");
        
        return supabaseWebClient.post()
                .uri("/" + table)
                .header("Prefer", "return=representation") // Request to return the inserted row
                .bodyValue(dataArray)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            System.out.println("=== SUPABASE ERROR ===");
                            System.out.println("Status: " + response.statusCode());
                            System.out.println("Error body: " + errorBody);
                            System.out.println("======================");
                            return new RuntimeException("Supabase error: " + errorBody);
                        })
                )
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Supabase response: " + response))
                .map(json -> {
                    try {
                        // PostgREST returns an array, extract first element
                        com.fasterxml.jackson.databind.JsonNode arrayNode = objectMapper.readTree(json);
                        if (arrayNode.isArray() && arrayNode.size() > 0) {
                            return objectMapper.treeToValue(arrayNode.get(0), clazz);
                        }
                        throw new RuntimeException("Empty response from Supabase");
                    } catch (Exception e) {
                        System.out.println("Error parsing JSON: " + json);
                        throw new RuntimeException("Error parsing JSON response: " + json, e);
                    }
                })
                .doOnError(error -> System.out.println("Supabase insert error: " + error.getMessage()));
    }

    /**
     * Выполнить UPDATE запрос
     */
    public <T> Mono<T> update(String table, T data, Map<String, String> filters) {
        return supabaseWebClient.patch()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/" + table);
                    // Добавляем фильтры как query параметры
                    for (Map.Entry<String, String> filter : filters.entrySet()) {
                        builder.queryParam(filter.getKey(), filter.getValue());
                    }
                    return builder.build();
                })
                .header("Prefer", "return=representation")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        // PostgREST returns an array for updates too
                        com.fasterxml.jackson.databind.JsonNode arrayNode = objectMapper.readTree(json);
                        if (arrayNode.isArray() && arrayNode.size() > 0) {
                            return objectMapper.treeToValue(arrayNode.get(0), (Class<T>) data.getClass());
                        }
                        // If empty array, return the original data
                        return data;
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing JSON response", e);
                    }
                });
    }

    /**
     * Выполнить DELETE запрос
     */
    public Mono<Void> delete(String table, Map<String, String> filters) {
        return supabaseWebClient.delete()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/" + table);
                    // Добавляем фильтры как query параметры
                    for (Map.Entry<String, String> filter : filters.entrySet()) {
                        builder.queryParam(filter.getKey(), filter.getValue());
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(Void.class);
    }

    /**
     * Выполнить RPC (Remote Procedure Call) - для вызова функций PostgreSQL
     */
    public <T> Mono<T> rpc(String functionName, Map<String, Object> params, Class<T> clazz) {
        return supabaseWebClient.post()
                .uri("/rpc/" + functionName)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, clazz);
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing JSON response", e);
                    }
                });
    }

    /**
     * Получить WebClient для прямого доступа
     */
    public WebClient getWebClient() {
        return supabaseWebClient;
    }
}
