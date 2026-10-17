package com.avnish.descriptAI_backend.controller;


import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;
import com.avnish.descriptAI_backend.dto.ProductDescriptionRequest;
import com.avnish.descriptAI_backend.service.AIService;
import com.avnish.descriptAI_backend.service.DescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/AI")
@RequiredArgsConstructor

public class AIGenerationController {

    private final AIService aiService;
    private final DescriptionService descriptionService;

        @PostMapping("/generate")
        public ResponseEntity<List<ProductDescriptionGeneratedResponse>> getProductById(@RequestBody ProductDescriptionRequest request, Principal principal) {
            log.info("search Product by Id from DB for Id: {}" , request.getPrompts());
            List<ProductDescriptionGeneratedResponse> results = aiService.generateDescription(request.getProductIds(), request.getPrompts());
            descriptionService.saveGenerated(results, principal.getName());
            return ResponseEntity.ok(results);
        }

    }
