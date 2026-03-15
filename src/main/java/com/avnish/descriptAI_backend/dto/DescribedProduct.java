package com.avnish.descriptAI_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DescribedProduct {

    private String productName;
    private String category;
    private String aiGeneratedDescription;
    private String imageURL;
}
