package com.avnish.descriptAI_backend.config;

import com.avnish.descriptAI_backend.client.AIClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.Optional;

@Configuration
public class AIClientConfig {

    @Value("${ai.provider}")
    private String provider;

    @Bean
    @Primary
    public AIClient aiClient(Map<String, AIClient> clients) {
        return Optional.ofNullable(clients.get(provider))
                .orElseThrow(() -> new RuntimeException("Invalid AI provider: " + provider));
    }
}
