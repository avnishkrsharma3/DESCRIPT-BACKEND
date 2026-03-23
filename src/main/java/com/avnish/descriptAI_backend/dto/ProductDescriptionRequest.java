package com.avnish.descriptAI_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDescriptionRequest {
    private List<String>productIds;
    private String prompts;
}
