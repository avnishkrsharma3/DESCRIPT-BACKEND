package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.client.GeminiApiClient;
import com.avnish.descriptAI_backend.dto.DescribedProduct;
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

    public List<DescribedProduct> generateDescription(int[] productIds, String prompts){
        List<DescribedProduct> describedProductList = new ArrayList<>();
        if(productIds == null || productIds.length == 0 || prompts == ""){
           DescribedProduct describedProduct = new DescribedProduct("", "",
                   "","");
           describedProductList.add(describedProduct);
           log.warn("No product selected ");
        }
        else{
            int  i = 0;
            for(int id: productIds){
                Product product = productRepository.findProductById(String.valueOf(id));
                DescribedProduct describedProduct = getDescriptionProduct(product, prompts);
                describedProductList.add(describedProduct);
                log.info("iteration no. - "  + i++);
            }
        }
        return describedProductList;
    }
    public DescribedProduct getDescriptionProduct(Product product, String prompts){
        DescribedProduct describedProduct;
        String productName = product.getTitle();
        String category = product.getCategory();
        String productDescription = product.getDescription();
        String [] promptSplit = prompts.split(",");
        String tone = promptSplit[0]; String length = promptSplit[1];
        String focus = promptSplit[2];
        String imgURL = product.getImages().get(0);
        ///  call should go to db and check is this already generated using AI
        if (checkIsGenerared((product.getId()))){
            ProductAIGenerated productAIGenerated = productAIGeneratedRepository.findProductAIGeneratedById(product.getId());
            describedProduct = new DescribedProduct(productName, category, productAIGenerated.getAiGeneratedDescription(), imgURL);
            return describedProduct;
        }
        log.info("Product " + product.getTitle() + " descripiton is not being generated yet.");

        String aiDescription = geminiApiClient.generateProductDescription(
                productName, category, productDescription, tone, length, focus);
        describedProduct = new DescribedProduct(productName, category, aiDescription, imgURL);
        return describedProduct;
    }
    public boolean checkIsGenerared(String id){
        log.info("product check inside prouduct ai generated");
        return productAIGeneratedRepository.existsById(id);
    }

}
