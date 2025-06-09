package com.project.bizconnect.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDto {
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private int stockQuantity;
    private Long storeId;
    private Long categoryId;
    private List<String> imageUrls = new ArrayList<>();
}
