package com.project.bizconnect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.ProductResponseDto;
import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.dto.CategoryResponseDto;
import com.project.bizconnect.service.ProductService;
import com.project.bizconnect.service.CategoryService;
import com.project.bizconnect.service.AdminService;
import com.project.bizconnect.service.StoreService;
import com.project.bizconnect.dto.CustomerStatsDto;
import com.project.bizconnect.dto.SellerStatsDto;
import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.dto.RoleChangeRequest;
import com.project.bizconnect.dto.StoreVerificationRequest;
import com.project.bizconnect.dto.StoreWithUserDto;
import com.project.bizconnect.entity.Role;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AdminService adminService;
    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<String> getAdmin() {
        return ResponseEntity.ok("admin route");
    }

    @GetMapping("/stores")
    public ResponseEntity<?> getAllStoresWithUsers() {
        return ResponseEntity.ok(adminService.getAllStoresWithUsers());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/role")
    public ResponseEntity<List<UserDto>> getUsersByRole(@RequestParam Role role) {
        List<UserDto> users = adminService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable int id, @RequestBody RoleChangeRequest request) {
        UserDto updated = adminService.changeUserRole(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductDto productDto) {
        if (productDto.getStoreId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Store ID is required");
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
        // Convert to response DTO with additional details
        ProductResponseDto response = productService.getProductByIdWithDetails(created.getProductId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        CategoryDto created = categoryService.createCategory(categoryDto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/stores/verification")
    public ResponseEntity<StoreWithUserDto> toggleStoreVerification(
            @RequestBody StoreVerificationRequest request) {
        StoreWithUserDto updatedStore = adminService.toggleStoreVerification(request.getStoreId(), request.isVerified());
        return ResponseEntity.ok(updatedStore);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProductsWithDetails();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductByIdWithDetails(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategoriesWithDetails();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/customers/stats")
    public ResponseEntity<List<CustomerStatsDto>> getAllCustomersWithStats() {
        List<CustomerStatsDto> customers = adminService.getAllCustomersWithStats();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/sellers/stores")
    public ResponseEntity<List<SellerStatsDto>> getAllSellersWithStores() {
        List<SellerStatsDto> sellers = adminService.getAllSellersWithStores();
        return ResponseEntity.ok(sellers);
    }
}
