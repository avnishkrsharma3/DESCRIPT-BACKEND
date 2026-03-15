package com.avnish.descriptAI_backend.controller;


import com.avnish.descriptAI_backend.client.GeminiApiClient;
import com.avnish.descriptAI_backend.dto.DescribedProduct;
import com.avnish.descriptAI_backend.dto.ProductDetails;
import com.avnish.descriptAI_backend.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/AI")
@RequiredArgsConstructor

public class AIGenerationController {

    private final GeminiApiClient geminiApiClient;
    private final AIService aiService;

    @PostMapping("/generate")
    public ResponseEntity<List<DescribedProduct>> getProductById(@RequestBody ProductDetails productDetails) {
        log.info("search Product by Id from DB for Id:" + productDetails.getPrompts());
        List<DescribedProduct> describedProductList;
        try {
            int [] productIds = productDetails.getProductIds();
            String prompts = productDetails.getPrompts();
            describedProductList = aiService.generateDescription(productIds, prompts);
            return ResponseEntity.ok(describedProductList);
        } catch (Exception e) {
            log.error("Error in searching and retrieving products by Id at Controller: " + e.getMessage());
        }
       return ResponseEntity.status(500).body( new ArrayList<>());
    }


}
