package com.avnish.descriptAI_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionGeneratedList {
    List<ProductDescriptionGeneratedResponse> ProductDescriptionGeneratedResponseList;
    List<String> productIds;
}
