package com.project.bizconnect.dto;

import lombok.Data;

@Data
public class ProductDto {
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private int stockQuantity;
    private boolean hasVariants;
    private Long storeId;
    private Long categoryId;
}
