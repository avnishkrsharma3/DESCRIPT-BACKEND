package com.avnish.descriptAI_backend.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiApiClient {

    @Value("${gemini.api.key}")  // ← Fixed: was gemini.api.url
    private String geminiApiKey;

    private Client client;

    @PostConstruct
    public void init() {
        log.info("Initializing Gemini AI Client");
        this.client = Client.builder()
                .apiKey(geminiApiKey)
                .build();
        log.info("Gemini AI Client initialized successfully");
    }

    /**
     * Generate product description using Gemini AI
     *
     * @param productName Name of the product
     * @param category Product category
     * @param productDescription
     * @return AI-generated description
     */
    public String generateProductDescription(String productName, String category, String productDescription, String tone
    , String length, String focus) {
        try {
            log.info("Generating description for product: {}", productName);

            // Build the prompt
            String prompt = buildProductDescriptionPrompt(productName, category, productDescription, tone, length,
                    focus);

            // Call Gemini API
            GenerateContentResponse response = client.models.generateContent(
                    "Gemini 2.5 Pro",  // Using stable model
                    prompt,
                    null
            );

            // Extract text from response
            String description = extractTextFromResponse(response);

            log.info("Successfully generated description for: {}", productName);
            log.info(description);
            return description;

        } catch (Exception e) {
            log.error("Error generating description for {}: {}", productName, e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI description: " + e.getMessage());
        }
    }




    /**
     * Build prompt for product description generation
     */
    private String buildProductDescriptionPrompt(String productName, String category, String productDescription,String tone
            , String length, String focus) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a compelling, " + focus +"-friendly product description");
        prompt.append("The description should be " + tone + ", highlight key features, and be of length " + "length " +
                "" + "\n\n");

        prompt.append("Product Name: ").append(productName).append("\n");

        if (category != null && !category.isEmpty()) {
            prompt.append("Category: ").append(category).append("\n");
        }

        if (productDescription != null && !productDescription.isEmpty()) {
            prompt.append("productDescription: ").append(productDescription).append("\n");
        }

        prompt.append("\nProvide only the description text without any labels, headers, or additional formatting.");

        return prompt.toString();
    }

    /**
     * Extract text from Gemini API response
     */
    private String extractTextFromResponse(GenerateContentResponse response) {
        try {
            String text = response.text();
            return text != null ? text : "Description not available";
        } catch (Exception e) {
            log.warn("No valid response from Gemini AI: {}", e.getMessage());
            return "Description not available";
        }
    }
}