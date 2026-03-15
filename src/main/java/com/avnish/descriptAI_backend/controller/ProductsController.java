package com.avnish.descriptAI_backend.controller;


import com.avnish.descriptAI_backend.client.GeminiApiClient;
import com.avnish.descriptAI_backend.client.ProductApiClient;
import com.avnish.descriptAI_backend.dto.ProductsResponse;
import com.avnish.descriptAI_backend.model.Product;
import com.avnish.descriptAI_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor

public class ProductsController {


    private final ProductApiClient productApiClient;
    private final ProductService productService;
    private final GeminiApiClient geminiApiClient;


    @GetMapping("")
    public ResponseEntity<ProductsResponse> getAllProducts(){
        log.info("get all product from DB");
        try {
            ProductsResponse productResponse = productService.getAllProducts();
           return ResponseEntity.ok(productResponse);
        } catch (Exception e) {
            log.error("Error in retrieving products at Controller: " + e.getMessage());
            return ResponseEntity.status(500).body(new ProductsResponse());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ProductsResponse> getAllProducts(@RequestParam("q") String searchQuery){
        log.info("search Products from DB");
        try {
            ProductsResponse productResponse = productService.searchProducts(searchQuery);
            return ResponseEntity.ok(productResponse);
        } catch (Exception e) {
            log.error("Error in searching and retrieving products at Controller: " + e.getMessage());
            return ResponseEntity.status(500).body(new ProductsResponse());
        }
    }
    /**
     * Fetch products from external API and save to MongoDB
     * Endpoint: POST /api/products/fetch
     */
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchProducts() {
        log.info("Fetch endpoint called");

        try {
            List<Product> products = productApiClient.fetchAndSaveProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Products fetched and saved successfully");
            response.put("totalSaved", products.size());
            response.put("products", products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in fetch endpoint: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch products: " + e.getMessage());
            errorResponse.put("totalSaved", 0);

            return ResponseEntity.status(500).body(errorResponse);
        }
    }




}
