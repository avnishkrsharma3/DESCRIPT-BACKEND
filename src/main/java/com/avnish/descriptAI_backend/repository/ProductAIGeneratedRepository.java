package com.avnish.descriptAI_backend.repository;

import com.avnish.descriptAI_backend.model.ProductAIGenerated;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductAIGeneratedRepository extends MongoRepository<ProductAIGenerated, String> {

    ProductAIGenerated findProductAIGeneratedById(String id);
}
