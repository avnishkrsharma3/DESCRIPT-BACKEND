package com.avnish.descriptAI_backend.controller;


import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;
import com.avnish.descriptAI_backend.dto.ProductDescriptionRequest;
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

    private final AIService aiService;


        @PostMapping("/generate")
        public ResponseEntity<List<ProductDescriptionGeneratedResponse>> getProductById(@RequestBody ProductDescriptionRequest ProductDescriptionRequest) {
            log.info("search Product by Id from DB for Id: {}" , ProductDescriptionRequest.getPrompts());
            List<ProductDescriptionGeneratedResponse> productDescriptionGeneratedResponseList;
            try {
                List<String> productIds = ProductDescriptionRequest.getProductIds();
                String prompts = ProductDescriptionRequest.getPrompts();
                productDescriptionGeneratedResponseList = aiService.generateDescription(productIds, prompts);
                return ResponseEntity.ok(productDescriptionGeneratedResponseList);
            } catch (Exception e) {
                log.error("Error in searching and retrieving products by Id at Controller: {}", e.getMessage());
            }
            return ResponseEntity.status(500).body( new ArrayList<>());
        }




    }
