package com.avnish.descriptAI_backend.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ProductAIGenerated")
public class ProductAIGenerated {
    @Id
    private String id;
    private String productName;
    private String category;
    private String aiGeneratedDescription;
    private String imageURL;
}
