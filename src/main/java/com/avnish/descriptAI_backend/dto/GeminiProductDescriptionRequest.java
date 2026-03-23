package com.avnish.descriptAI_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiProductDescriptionRequest {
    private int[] productIds;
    private String prompts;
}
