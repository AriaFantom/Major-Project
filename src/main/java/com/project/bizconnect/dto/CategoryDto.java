package com.project.bizconnect.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private Long storeId;
    private Long parentCategoryId;
}
