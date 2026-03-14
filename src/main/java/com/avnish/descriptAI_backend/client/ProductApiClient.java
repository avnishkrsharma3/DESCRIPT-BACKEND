package com.avnish.descriptAI_backend.client;

import com.avnish.descriptAI_backend.dto.ProductsResponse;
import com.avnish.descriptAI_backend.model.Product;
import com.avnish.descriptAI_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApiClient {

    @Autowired
    private RestTemplate restTemplate;

    private final ProductRepository productRepository;
    // Read from .env file via application.properties
    @Value("${external.api.url}")
    private String externalApiUrl;


    /**
     * Fetch products from external API and save to MongoDB
     */
    public List<Product> fetchAndSaveProducts() {
        try {
            log.info("Fetching products from external API: {}", externalApiUrl);

            // Call external API and get Product array directly
             ProductsResponse productsResponse = restTemplate.getForObject(externalApiUrl, ProductsResponse.class);

             if(productsResponse == null || productsResponse.getProducts() == null || productsResponse.getProducts().size() == 0)
             {
                 log.warn("No Product received from external API");
                 return List.of();
             }
            List<Product> productsList = productsResponse.getProducts();
            // Convert array to list
            log.info("Fetched {} products from external API", productsList.size());

            // Save all products to MongoDB
            List<Product> savedProducts = productRepository.saveAll(productsList);
            log.info("Saved {} products to MongoDB", savedProducts.size());

            return savedProducts;

        } catch (Exception e) {
            log.error("Error fetching and saving products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products from external API", e);
        }
    }

    /**
     * Get count of products in database
     */
    public long getProductCount() {
        return productRepository.count();
    }
}