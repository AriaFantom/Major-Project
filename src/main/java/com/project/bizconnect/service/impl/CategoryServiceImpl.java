package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.entity.Category;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.repository.CategoryRespository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
}

