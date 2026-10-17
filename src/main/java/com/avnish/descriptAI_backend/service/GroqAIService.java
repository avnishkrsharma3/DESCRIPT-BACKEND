package com.avnish.descriptAI_backend.service;

import com.avnish.descriptAI_backend.constants.DescriptionStatus;
import com.avnish.descriptAI_backend.dto.*;
import com.avnish.descriptAI_backend.model.Product;
import com.avnish.descriptAI_backend.model.ProductDescription;
import com.avnish.descriptAI_backend.repository.ProductDescriptionRepository;
import com.avnish.descriptAI_backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor          // was @AllArgsConstructor: it would also try to inject the @Value field below
public class GroqAIService {

    /** Status of a result that was just generated and is NOT saved yet. DescriptionService saves only these. */
    public static final String STATUS_NEW = "GENERATED";

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final ProductDescriptionRepository productDescriptionRepository;   // was "productionDescriptionRepository"

    // Set groq.model to the model your AIClient really calls, so the saved aiModel is correct
    @Value("${groq.model:openai/gpt-oss-120b}")
    private String aiModel;

    /**
     * Parse GroqResponse and convert to List<ProductDescriptionGeneratedResponse>
     */
    public List<ProductDescriptionGeneratedResponse> parseToProductDescriptions(GroqResponse groqResponse) {
        try {
            // Extract text content
            String outputType  = "message";
            String outputText = "output_text";
            String textContent = groqResponse.getOutput().stream()
                    .filter(output -> outputType.equals(output.getType()))
                    .flatMap(output -> output.getContent().stream())
                    .filter(content -> outputText.equals(content.getType()))
                    .map(Content::getText)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No text content found"));

            log.info("Extracted JSON text: {}", textContent);

            // Parse inner JSON
            String cleanedJson = textContent.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            AIProductResponse aiProductResponse = objectMapper.readValue(cleanedJson, AIProductResponse.class);

            // build response
            List<ProductDescriptionGeneratedResponse> responses = buildResponse(aiProductResponse);

            log.info("Parsed {} product descriptions", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("Error parsing GroqResponse: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    /**
     * Get originalDescription, imgUrl and category from DB using product Id
     */
    private List<ProductDescriptionGeneratedResponse> buildResponse(AIProductResponse aiProductResponse) {

        log.info("BUILDING GROQ AI GENERATED RESPONSE");
        if (aiProductResponse == null || aiProductResponse.getGroqAIProductList() == null) {
            log.info("aiProductResponse is empty");
            return new ArrayList<>();
        }

        List<ProductDescriptionGeneratedResponse> responses = new ArrayList<>();
        for (GroqAIProduct groqAIProduct : aiProductResponse.getGroqAIProductList()) {
            Optional<Product> optionalProduct = productRepository.findById(groqAIProduct.getId());
            if (optionalProduct.isPresent()) {
                responses.add(createProductDescriptionGeneratedResponse(optionalProduct.get(), groqAIProduct));
            } else {
                log.error("Product not found for id {}", groqAIProduct.getId());
            }
        }
        return responses;
    }

    /**
     * A description that is ALREADY SAVED (APPROVED or DRAFT): build the response from the saved document.
     */
    public ProductDescriptionGeneratedResponse createProductDescriptionGeneratedResponse(ProductDescription saved) {
        LocalDateTime createdTime = (saved.getCreatedAt() == null)
                ? null
                : LocalDateTime.ofInstant(saved.getCreatedAt(), IST);          // Instant -> IST LocalDateTime

        return new ProductDescriptionGeneratedResponse(
                String.valueOf(saved.getProductId()),         // productId (Long in DB, String in the response)
                saved.getProductName(),                       // productName
                saved.getCategory(),                          // category
                saved.getOriginalDescription(),               // originalDescription
                saved.getAiGeneratedDescription(),            // aiGeneratedDescription
                saved.getAiModel(),                           // aiModel
                saved.getImageURL(),                          // imageURL
                createdTime,                                  // createdTime
                saved.getStatus().name(),                     // status: "APPROVED" or "DRAFT"
                saved.getStatus() == DescriptionStatus.APPROVED   // approved
        );
    }

    /**
     * A description the AI JUST generated (not saved yet). Status is STATUS_NEW so that
     * DescriptionService knows it still has to be saved as a DRAFT.
     */
    public ProductDescriptionGeneratedResponse createProductDescriptionGeneratedResponse(Product product, GroqAIProduct groqAIProduct) {
        return new ProductDescriptionGeneratedResponse(
                product.getId(),                              // productId
                product.getTitle(),                           // productName
                product.getCategory(),                        // category
                product.getDescription(),                     // originalDescription
                groqAIProduct.getDescription(),               // aiGeneratedDescription
                aiModel,                                      // aiModel
                firstImage(product),                          // imageURL
                LocalDateTime.now(IST),                       // createdTime (IST)
                STATUS_NEW,                                   // status
                false                                         // approved
        );
    }

    /** First image, else the thumbnail, else null (never throws on an empty list). */
    private String firstImage(Product product) {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            return product.getImages().get(0);
        }
        return product.getThumbnail();
    }

    /**
     * Generate AI prompt content
     *
     * @param productIds Array of product IDs
     * @param descriptionType Type of description (e.g., "seo", "short", "seo,short")
     * @return Formatted prompt string
     */
    public String buildPromptContent(List<String> productIds, String descriptionType) {

        // Fetch products from database
        List<Product> products = productRepository.findAllById(productIds);

        if (products.isEmpty()) {
            throw new RuntimeException("No products found for given IDs");
        }

        // Determine description requirements based on type
        String requirements = getRequirements(descriptionType);

        // Build prompt
        StringBuilder content = new StringBuilder();
        content.append("Generate ").append(requirements).append(" product descriptions.\n");   // FIX: was missing ".\n"
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

        // custom prompt "tone,length,focus"
        String[] promptSplit = descriptionType.split(",");
        if (promptSplit.length < 3) {                      // FIX: was ArrayIndexOutOfBoundsException
            return descriptionType.trim();
        }
        String tone = promptSplit[0].trim();
        String length = promptSplit[1].trim();
        String focus = promptSplit[2].trim();

        return tone + " " + length + " and " + focus;
    }

    /**
     * For each product id: APPROVED description if one exists, otherwise the latest DRAFT,
     * otherwise it goes in notFoundList (the AI has to generate it).
     * Older versions are ignored.
     */
    public DescriptionGeneratedList findGeneratedOne(List<String> productIds) {
        List<ProductDescriptionGeneratedResponse> found = new ArrayList<>();
        List<String> notFoundList = new ArrayList<>();

        for (String id : productIds) {
            Long productId;
            try {
                productId = Long.parseLong(id.trim());
            } catch (NumberFormatException | NullPointerException e) {
                log.warn("Invalid product id '{}', treated as not found", id);
                notFoundList.add(id);
                continue;
            }

            // 1st choice: the approved one
            Optional<ProductDescription> chosen =
                    productDescriptionRepository.findByProductIdAndStatus(productId, DescriptionStatus.APPROVED);

            // 2nd choice: the newest draft
            if (chosen.isEmpty()) {
                chosen = productDescriptionRepository
                        .findAllByProductIdAndStatus(productId, DescriptionStatus.DRAFT).stream()
                        .max(Comparator.comparingInt(ProductDescription::getVersionNumber));
            }

            if (chosen.isPresent()) {
                log.info("Existing {} description for product {}", chosen.get().getStatus(), productId);
                found.add(createProductDescriptionGeneratedResponse(chosen.get()));
            } else {
                notFoundList.add(id);
            }
        }
        return new DescriptionGeneratedList(found, notFoundList);
    }
}