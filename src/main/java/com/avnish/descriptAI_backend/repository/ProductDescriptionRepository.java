package com.avnish.descriptAI_backend.repository;

import com.avnish.descriptAI_backend.constants.DescriptionStatus;
import com.avnish.descriptAI_backend.model.ProductDescription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductDescriptionRepository extends MongoRepository<ProductDescription, String> {

    // newest version of a product (empty if none yet) -> next version = this + 1
    Optional<ProductDescription> findFirstByProductIdOrderByVersionNumberDesc(Long productId);

    // the live description, if any
    Optional<ProductDescription> findByProductIdAndStatus(Long productId, DescriptionStatus status);

    // pending drafts for a product
    List<ProductDescription> findAllByProductIdAndStatus(Long productId, DescriptionStatus status);

    // full history, newest first
    List<ProductDescription> findByProductIdOrderByVersionNumberDesc(Long productId);

    // full history, newest first
    List<ProductDescription> findAll();

}