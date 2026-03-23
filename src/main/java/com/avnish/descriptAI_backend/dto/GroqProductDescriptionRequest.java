package com.avnish.descriptAI_backend.dto;

import lombok.Data;

@Data
public class GroqProductDescriptionRequest {
    private String id;          // Product ID
    private String name;
    private String description; // SEO description
    private String prompt;
}
