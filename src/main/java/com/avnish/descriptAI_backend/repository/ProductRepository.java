package com.avnish.descriptAI_backend.repository;


import com.avnish.descriptAI_backend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Search across multiple fields
    @Query("{ $or: [ " +
            "{ 'title': { $regex: ?0, $options: 'i' } }, " +
            "{ 'description': { $regex: ?0, $options: 'i' } }, " +
            "{ 'brand': { $regex: ?0, $options: 'i' } }, " +
            "{ 'category': { $regex: ?0, $options: 'i' } }, " +
            "{ 'tags': { $regex: ?0, $options: 'i' } } " +
            "] }")
    List<Product> searchProducts(String query);
}