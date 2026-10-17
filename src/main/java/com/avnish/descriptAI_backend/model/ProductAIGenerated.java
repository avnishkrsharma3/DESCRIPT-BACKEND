package com.avnish.descriptAI_backend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAIGenerated {
    private String productId;
    private String productName;
    private String category;
    private String originalDescription;
    private String aiGeneratedDescription;
    private String aiModel;
    private String imageURL;
    private LocalDateTime createdTime;
    private LocalDateTime approvedTime;
    private String status;
    private String productApprovedBy;
}