package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.client.AIClient;
import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AIService {


    private final AIClient aiClient;

    public List<ProductDescriptionGeneratedResponse> generateDescription(List<String> productIds, String prompts) {
        if (productIds == null || productIds.isEmpty() || prompts == null || prompts.isBlank()) {
            log.warn("No products or prompts provided");
            return List.of();
        } else {
            return aiClient.generateProductDescription(productIds, prompts);
        }
    }


}
