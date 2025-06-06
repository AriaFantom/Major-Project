package com.project.bizconnect.dto;

import lombok.Data;

@Data
public class CategoryResponseDto {
    private Long id;
    private String name;
    private Long storeId;
    private String storeName;
    private Long parentCategoryId;
    private String parentCategoryName;
}
