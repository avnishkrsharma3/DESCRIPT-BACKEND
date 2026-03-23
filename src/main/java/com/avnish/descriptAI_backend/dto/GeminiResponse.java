package com.avnish.descriptAI_backend.dto;

import com.avnish.descriptAI_backend.model.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse{
    private List<Product> products;
    private Integer total;
    private Integer skip;
    private Integer limit;
}
