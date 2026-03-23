package com.avnish.descriptAI_backend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "ProductAIGenerated")
@AllArgsConstructor
@NoArgsConstructor
public class ProductAIGenerated {
    @Id
    private String productId;
    private String productName;
    private String category;
    private String originalDescription;
    private String aiGeneratedDescription;
    private String aiModel;
    private String imageURL;
    private LocalDateTime createdTime;
}
