package com.avnish.descriptAI_backend.dto;


import lombok.Data;

import java.util.List;

@Data
public class AIProductResponse {
    private List<GroqAIProduct> products;
}

