package com.avnish.descriptAI_backend.client;

import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;

import java.util.List;

public interface AIClient {
    public List<ProductDescriptionGeneratedResponse> generateProductDescription(List<String> productIds, String prompt);
}
