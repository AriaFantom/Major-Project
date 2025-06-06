package com.project.bizconnect.service;

import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.dto.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();
    CategoryDto getCategoryById(Long id);
    CategoryDto createCategory(CategoryDto categoryDto);
    List<CategoryDto> getCategoriesByStoreId(Long storeId);

    // New methods returning the enhanced response DTO
    List<CategoryResponseDto> getAllCategoriesWithDetails();
    CategoryResponseDto getCategoryByIdWithDetails(Long id);
    List<CategoryResponseDto> getCategoriesByStoreIdWithDetails(Long storeId);

    // New method for secured category creation
    CategoryResponseDto createCategoryForStore(CategoryDto categoryDto, boolean isStoreOwner);

    // Check if a category belongs to a specific store
    boolean isCategoryInStore(Long categoryId, Long storeId);
}
