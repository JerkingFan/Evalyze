package org.example.new_new_mvp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RepositoryConfig {

    @Value("${app.repository.type:jpa}")
    private String repositoryType;

    @Bean
    @Primary
    public String repositoryType() {
        return repositoryType;
    }

    public boolean useJpaRepositories() {
        return "jpa".equalsIgnoreCase(repositoryType);
    }

    public boolean useSupabaseRepositories() {
        return "supabase".equalsIgnoreCase(repositoryType);
    }
}
