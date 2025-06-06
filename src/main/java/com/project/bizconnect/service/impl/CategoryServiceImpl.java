package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.dto.CategoryResponseDto;
import com.project.bizconnect.entity.Category;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.repository.CategoryRespository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRespository categoryRespository;
    private final StoreRepository storeRepository;

    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setStoreId(category.getStore().getStoreId());
        dto.setParentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null);
        return dto;
    }

    // New method to map Category to CategoryResponseDto with additional details
    private CategoryResponseDto mapToResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());

        // Store details
        dto.setStoreId(category.getStore().getStoreId());
        dto.setStoreName(category.getStore().getStoreName());

        // Parent category details
        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
            dto.setParentCategoryName(category.getParentCategory().getName());
        }

        return dto;
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRespository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRespository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return mapToDto(category);
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        Category category = new Category();
        category.setName(dto.getName());
        category.setStore(store);
        if (dto.getParentCategoryId() != null) {
            Category parent = categoryRespository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            category.setParentCategory(parent);
        }
        Category saved = categoryRespository.save(category);
        return mapToDto(saved);
    }

    @Override
    public List<CategoryDto> getCategoriesByStoreId(Long storeId) {
        // Using the direct repository method to find categories by store ID
        return categoryRespository.findByStoreStoreId(storeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // New methods that return the enhanced response DTO
    @Override
    public List<CategoryResponseDto> getAllCategoriesWithDetails() {
        return categoryRespository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto getCategoryByIdWithDetails(Long id) {
        Category category = categoryRespository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return mapToResponseDto(category);
    }

    @Override
    public List<CategoryResponseDto> getCategoriesByStoreIdWithDetails(Long storeId) {
        return categoryRespository.findByStoreStoreId(storeId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    // New method for secured category creation
    @Override
    public CategoryResponseDto createCategoryForStore(CategoryDto dto, boolean isStoreOwner) {
        // Check if user is the store owner
        if (!isStoreOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to add categories to this store");
        }

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        Category category = new Category();
        category.setName(dto.getName());
        category.setStore(store);

        if (dto.getParentCategoryId() != null) {
            Category parent = categoryRespository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));
            category.setParentCategory(parent);
        }

        Category saved = categoryRespository.save(category);
        return mapToResponseDto(saved);
    }

    @Override
    public boolean isCategoryInStore(Long categoryId, Long storeId) {
        Optional<Category> categoryOptional = categoryRespository.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }

        Category category = categoryOptional.get();
        return category.getStore().getStoreId().equals(storeId);
    }
}
