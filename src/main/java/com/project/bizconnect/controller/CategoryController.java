package com.project.bizconnect.controller;

import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByStoreId(@PathVariable Long storeId) {
        return ResponseEntity.ok(categoryService.getCategoriesByStoreId(storeId));
    }
}
