package com.avnish.descriptAI_backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String title;
    private String description;
    private String category;
    private Double price;
    private Double discountPercentage;
    private Double rating;
    private Integer stock;
    private List<String> tags;
    private String brand;
    private String sku;
    private Integer weight;

    private Dimensions dimensions;
    private String warrantyInformation;
    private String shippingInformation;
    private String availabilityStatus;
    private List<Review> reviews;
    private String returnPolicy;
    private Integer minimumOrderQuantity;
    private Meta meta;
    private List<String> images;
    private String thumbnail;

    @Data
    public static class Dimensions {
        private Double width;
        private Double height;
        private Double depth;
    }

    @Data
    public static class Review {
        private Integer rating;
        private String comment;
        private LocalDateTime date;
        private String reviewerName;
        private String reviewerEmail;
    }

    @Data
    public static class Meta {
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String barcode;
        private String qrCode;
    }
}