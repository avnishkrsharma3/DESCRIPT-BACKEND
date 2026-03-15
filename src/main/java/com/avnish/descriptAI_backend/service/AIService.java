package com.avnish.descriptAI_backend.service;


import com.avnish.descriptAI_backend.client.GeminiApiClient;
import com.avnish.descriptAI_backend.dto.DescribedProduct;
import com.avnish.descriptAI_backend.model.Product;
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
                Product product = productRepository.findProductById(id+"");
                DescribedProduct describedProduct = getDescription(product, prompts);
                describedProductList.add(describedProduct);
                log.info("iteration no. - "  + i++);
            }
        }
        return describedProductList;
    }
    public DescribedProduct getDescription(Product product, String prompts){
        String productName = product.getTitle();
        String category = product.getCategory();
        String productDescription = product.getDescription();
        String [] promptSplit = prompts.split(",");
        String tone = promptSplit[0]; String length = promptSplit[1];
        String focus = promptSplit[2];
        String imgURL = product.getImages().get(0);
        String aiDescription = geminiApiClient.generateProductDescription(
                productName, category, productDescription, tone, length, focus);
        DescribedProduct describedProduct = new DescribedProduct(productName, category, aiDescription, imgURL);
        return describedProduct;
    }

}
