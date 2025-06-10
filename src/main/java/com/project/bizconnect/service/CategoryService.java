package com.project.bizconnect.service;

import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.dto.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();
    CategoryDto getCategoryById(Long id);
    CategoryDto createCategory(CategoryDto categoryDto);
    List<CategoryDto> getCategoriesByStoreId(Long storeId);
    List<CategoryResponseDto> getAllCategoriesWithDetails();
    List<CategoryResponseDto> getCategoriesByStoreIdWithDetails(Long storeId);
    CategoryResponseDto createCategoryForStore(CategoryDto categoryDto, boolean isStoreOwner);
    boolean isCategoryInStore(Long categoryId, Long storeId);
}
