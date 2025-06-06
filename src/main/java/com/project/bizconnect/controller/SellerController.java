package com.project.bizconnect.controller;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.service.CategoryService;
import com.project.bizconnect.service.ProductService;
import com.project.bizconnect.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final StoreService storeService;
    private final CategoryService categoryService;
    private final ProductService productService;

    @PostMapping("/stores")
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto) {
        StoreDto created = storeService.createStore(storeDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/stores")
    public ResponseEntity<List<StoreDto>> getMyStores() {
        List<StoreDto> stores = storeService.getStoresByAuthenticatedSeller();
        return ResponseEntity.ok(stores);
    }


    @GetMapping
    public ResponseEntity<String> getSeller() {
        return ResponseEntity.ok("seller route");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDto>> getCategoriesByStoreId(@RequestParam Long storeId) {
        List<CategoryResponseDto> categories = categoryService.getCategoriesByStoreIdWithDetails(storeId);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponseDto> addCategory(@RequestBody CategoryDto categoryDto) {
        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(categoryDto.getStoreId());
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only add categories to stores you own");
        }
        CategoryResponseDto createdCategory = categoryService.createCategoryForStore(categoryDto, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        if (productDto.getStoreId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Store ID is required");
        }
        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(productDto.getStoreId());
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only add products to stores you own");
        }
        if (productDto.getCategoryId() != null) {
            boolean categoryBelongsToStore = categoryService.isCategoryInStore(
                    productDto.getCategoryId(), productDto.getStoreId());
            if (!categoryBelongsToStore) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The specified category does not belong to the specified store");
            }
        }
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getProductsByStore(@RequestParam Long storeId) {
        // Verify store ownership
        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(storeId);
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view products for stores you own");
        }

        List<ProductResponseDto> products = productService.getProductsByStoreIdWithDetails(storeId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long productId) {
        ProductResponseDto product = productService.getProductByIdWithDetails(productId);

        // Verify store ownership
        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(product.getStoreId());
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view products from stores you own");
        }

        return ResponseEntity.ok(product);
    }
}
