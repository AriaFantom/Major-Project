package com.project.bizconnect.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductResponseDto {
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private int stockQuantity;

    // Store details
    private Long storeId;
    private String storeName;

    // Category details
    private Long categoryId;
    private String categoryName;

    // Image URLs
    private List<String> imageUrls = new ArrayList<>();
}
