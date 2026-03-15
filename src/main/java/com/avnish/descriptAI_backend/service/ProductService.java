package com.avnish.descriptAI_backend.service;

import com.avnish.descriptAI_backend.dto.ProductsResponse;
import com.avnish.descriptAI_backend.model.Product;
import com.avnish.descriptAI_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Get all products with limit
    public ProductsResponse getAllProducts() {
        List<Product> allProducts = productRepository.findAll();

        return new ProductsResponse(
                allProducts,
                allProducts.size(),
                0,
                200
        );
    }

    // Search products
    public ProductsResponse searchProducts(String query) {

        List<Product> searchResults = productRepository.searchProducts(query);

        return new ProductsResponse(
                searchResults,
                searchResults.size(),
                0,
                searchResults.size()
        );
    }

    // search product by id
    public Product searchProductById(String query){
        Product product = productRepository.findProductById(query);
            return product;
    }
}