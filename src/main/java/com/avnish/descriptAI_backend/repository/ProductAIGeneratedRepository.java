package com.avnish.descriptAI_backend.repository;

import com.avnish.descriptAI_backend.model.ProductAIGenerated;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductAIGeneratedRepository extends MongoRepository<ProductAIGenerated, String> {

    ProductAIGenerated findProductAIGeneratedByProductId(String id);
    ProductAIGenerated save(ProductAIGenerated productAIGenerated);
    List<ProductAIGenerated> findAll();
}
