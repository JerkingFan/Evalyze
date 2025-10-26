package org.example.new_new_mvp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url:https://fqyklholxklhwydksazc.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.anon.key:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZxeWtsaG9seGtsaHd5ZGtzYXpjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTU2OTMwOTcsImV4cCI6MjA3MTI2OTA5N30.eswlOcOidlZ1V9-32Xne9TJFptNhs1s_aYJ_m6mQmps}")
    private String supabaseAnonKey;

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", supabaseAnonKey)
                .defaultHeader("Authorization", "Bearer " + supabaseAnonKey)
                .defaultHeader("Content-Type", "application/json")
                // Don't set default Prefer header - let each request specify it
                .build();
    }

    @Bean
    public String supabaseUrl() {
        return supabaseUrl;
    }

    @Bean
    public String supabaseAnonKey() {
        return supabaseAnonKey;
    }
}
