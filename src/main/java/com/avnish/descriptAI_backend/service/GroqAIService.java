package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.dto.*;
import com.avnish.descriptAI_backend.model.Product;
import com.avnish.descriptAI_backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class GroqAIService {

    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;


    /**
     * Parse GroqResponse and convert to List<ProductDescriptionGeneratedResponse>
     */
    public List<ProductDescriptionGeneratedResponse> parseToProductDescriptions(GroqResponse groqResponse) {
        try {
            // Extract text content
            String textContent = groqResponse.getOutput().stream()
                    .filter(output -> "message".equals(output.getType()))
                    .flatMap(output -> output.getContent().stream())
                    .filter(content -> "output_text".equals(content.getType()))
                    .map(Content::getText)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No text content found"));

            log.debug("Extracted JSON text: {}", textContent);

            // Parse inner JSON
            AIProductResponse aiResponse = objectMapper.readValue(textContent, AIProductResponse.class);

            // Convert to ProductDescriptionGeneratedResponse list
            List<String> productIds = aiResponse.getProducts().stream()
                    .map(GroqAIProduct::getId) // Method reference to your getter
                    .toList();
            List<ProductDescriptionGeneratedResponse> responses = aiResponse.getProducts().stream()
                    .map(aiProduct -> new ProductDescriptionGeneratedResponse(
                            aiProduct.getId(),                    // productId
                            aiProduct.getName(),                  // productName
                            "",                                 // category (empty for now)
                            "",                                 // originalDescription (empty for now)
                            aiProduct.getDescription(),           // aiGeneratedDescription
                            "openai/gpt-oss-120b",               // aiModel
                            ""                                  // imageURL (empty for now)
                    ))
                    .toList();

            log.info("Parsed {} product descriptions", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("Error parsing GroqResponse: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
        /**
         * Generate AI prompt content
         *
         * @param productIds Array of product IDs
         * @param descriptionType Type of description (e.g., "seo", "short", "seo,short")
         * @return Formatted prompt string
         */
        public String buildPromptContent(List<String>productIds, String descriptionType) {

            // Fetch products from database
            List<Product> products = productRepository.findAllById(productIds);

            if (products.isEmpty()) {
                throw new RuntimeException("No products found for given IDs");
            }

            // Determine description requirements based on type
            String requirements = getRequirements(descriptionType);

            // Build prompt
            StringBuilder content = new StringBuilder();
            content.append("Generate ").append(requirements).append(" product descriptions");
            content.append("Return ONLY valid JSON. No markdown, no extra text.\n\n");

            content.append("Requirements:\n");

            if (descriptionType.contains("seo")) {
                content.append("- Include SEO keywords naturally\n");
                content.append("- Highlight benefits and features\n");
            }

            if (descriptionType.contains("short")) {
                content.append("- Concise and impactful\n");
            }

            content.append("- Professional, engaging tone\n\n");

            content.append("Format:\n");
            content.append("{\n");
            content.append("  \"products\": [\n");
            content.append("    {\"id\":\"product_id\",\"name\":\"product_name\",\"description\":\"SEO description here\"}\n");
            content.append("  ]\n");
            content.append("}\n\n");

            content.append("Products:\n");

            // Add product details
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                content.append(String.format("%d. ID: %s\n", i + 1, p.getId()));
                content.append(String.format("   Name: %s\n", p.getTitle()));
                content.append(String.format("   Category: %s\n", p.getCategory()));

                if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                    content.append(String.format("   Current: %s\n", p.getDescription()));
                }

                content.append("\n");
            }

            log.debug("Generated prompt content: {}", content);
            return content.toString();
        }

        /**
         * Get requirements text based on description type
         */
        private String getRequirements(String descriptionType) {
            if (descriptionType.contains("seo") && descriptionType.contains("short")) {
                return "SEO-friendly short";
            } else if (descriptionType.contains("seo")) {
                return "SEO-friendly";
            } else if (descriptionType.contains("short")) {
                return "short";
            }

            //
            String [] promptSplit = descriptionType.split(",");
            String tone = promptSplit[0]; String length = promptSplit[1];
            String focus = promptSplit[2];

            return tone+" " + length + " and " + focus;
        }

        /**
         * Get word count range based on description type
         */
        private String getWordCount(String descriptionType) {
            if (descriptionType.contains("short")) {
                return " (50-80 words each)";
            } else if (descriptionType.contains("seo")) {
                return " (80-120 words each)";
            }
            return " (80-120 words each)";
        }
    }



