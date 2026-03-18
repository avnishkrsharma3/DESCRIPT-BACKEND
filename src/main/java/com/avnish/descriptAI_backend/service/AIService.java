package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.client.GeminiApiClient;
import com.avnish.descriptAI_backend.model.Product;
import com.avnish.descriptAI_backend.model.ProductAIGenerated;
import com.avnish.descriptAI_backend.repository.ProductAIGeneratedRepository;
import com.avnish.descriptAI_backend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AIService {


    private final GeminiApiClient geminiApiClient;
    private final ProductRepository productRepository;
    private final ProductAIGeneratedRepository productAIGeneratedRepository;

    public List<ProductAIGenerated> generateDescription(int[] productIds, String prompts){
        List<ProductAIGenerated> ProductAIGeneratedList = new ArrayList<>();
        if(productIds == null || productIds.length == 0 || prompts == ""){
            ProductAIGenerated ProductAIGenerated = new ProductAIGenerated();
           ProductAIGeneratedList.add(ProductAIGenerated);
           log.warn("No product selected ");
        }
        else{
            int  i = 1;
            for(int id: productIds){
                Product product = productRepository.findProductById(String.valueOf(id));
                ProductAIGenerated ProductAIGenerated = getDescriptionProduct(product, prompts);
                ProductAIGeneratedList.add(ProductAIGenerated);
                log.info("iteration no. - "  + i++);
            }
        }
        return ProductAIGeneratedList;
    }
    public ProductAIGenerated getDescriptionProduct(Product product, String prompts){
        ProductAIGenerated productAIGenerated;
        String productName = product.getTitle();
        String category = product.getCategory();
        String productDescription = product.getDescription();
        String [] promptSplit = prompts.split(",");
        String tone = promptSplit[0]; String length = promptSplit[1];
        String focus = promptSplit[2];
        String imgURL = product.getImages().get(0);
        ///  call should go to db and check is this already generated using AI
        if (checkIsGenerared((product.getId()))){
             productAIGenerated = productAIGeneratedRepository.findProductAIGeneratedById(product.getId());
            return productAIGenerated;
        }
        log.info("Product " + product.getTitle() + " descripiton is being generated using AI API.");

        String aiDescription = geminiApiClient.generateProductDescription(
                productName, category, productDescription, tone, length, focus);
        productAIGenerated = new ProductAIGenerated(product.getId(),productName, category, aiDescription, imgURL);
        return productAIGenerated;
    }
    public boolean checkIsGenerared(String id){
        log.info("product check inside prouduct ai generated");
        return productAIGeneratedRepository.existsById(id);
    }

    public boolean save(ProductAIGenerated productAIGenerated){
        log.info("saving approved description into DB, product name : " + productAIGenerated.getProductName());
        productAIGeneratedRepository.save(productAIGenerated);
        return true;
    }
}
