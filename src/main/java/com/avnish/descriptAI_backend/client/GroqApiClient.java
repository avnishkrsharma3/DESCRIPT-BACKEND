package com.avnish.descriptAI_backend.client;

import com.avnish.descriptAI_backend.dto.GroqAIRequest;
import com.avnish.descriptAI_backend.dto.GroqResponse;
import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;
import com.avnish.descriptAI_backend.service.GroqAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service("groq")
@Slf4j
@RequiredArgsConstructor
public class GroqApiClient implements AIClient{

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final GroqAIService groqAIService;


    @Override
    public List<ProductDescriptionGeneratedResponse> generateProductDescription(List<String> productIds, String prompts) {
        String content = groqAIService.buildPromptContent(productIds, prompts);
        return callGroqAPI(buildRequest(content));
    }

    // -------- 1. Build Request --------
    private HttpEntity<GroqAIRequest> buildRequest(String content) {

        GroqAIRequest request = GroqAIRequest.builder()
                .model("openai/gpt-oss-120b")
                .input(List.of(
                        GroqAIRequest.Input.builder()
                                .role("user")
                                .content(content)
                                .build()
                ))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    // -------- 2. Call API --------
    private List<ProductDescriptionGeneratedResponse> callGroqAPI(HttpEntity<GroqAIRequest> entity) {

        ResponseEntity<GroqResponse> response = restTemplate.postForEntity(apiUrl, entity, GroqResponse.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            GroqResponse groqData = response.getBody();
            log.info("Successfully retrieved description from Groq");
            return groqAIService.parseToProductDescriptions(groqData);

        } else {
            log.error("Failed to fetch from Groq. Status: {}", response.getStatusCode());
        }
        return List.of();
    }



}
