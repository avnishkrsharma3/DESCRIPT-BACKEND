package com.avnish.descriptAI_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "product_descriptionsAIGenerated")
@AllArgsConstructor
@NoArgsConstructor
public class ProductAIGenerated {
    @Id
    private String id;

    private String productId;
    private String productName;

    // Original description
    private String originalDescription;

    // AI Generated SEO description
    private String seoDescription;

    // Metadata
    private LocalDateTime generatedAt;
    private String aiModel;
    private Boolean approved;
    private LocalDateTime approvedAt;
}
