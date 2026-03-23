package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.client.AIClient;
import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;
import com.avnish.descriptAI_backend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AIService {


    private final AIClient aiClient;
    private final ProductRepository productRepository;

    public List<ProductDescriptionGeneratedResponse> generateDescription(List<String> productIds, String prompts){
        List<ProductDescriptionGeneratedResponse> productDescriptionListGeneratedResponse = new ArrayList<>();
        if (productIds == null || productIds.size() == 0 || prompts == null || prompts.isBlank()) {
            log.warn("No products or prompts provided");
            return List.of();
        }
        else{
              return aiClient.generateProductDescription(productIds, prompts);
            }
    }



}
