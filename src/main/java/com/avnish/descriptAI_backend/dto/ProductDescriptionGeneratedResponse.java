
package com.avnish.descriptAI_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// This will be used to send data to frontend
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDescriptionGeneratedResponse {
    private String productId;
    private String productName;
    private String category;
    private String originalDescription;
    private String aiGeneratedDescription;
    private String aiModel;
    private String imageURL;
    private LocalDateTime createdTime;
    private boolean approved;
}
