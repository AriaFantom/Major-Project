package com.project.bizconnect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.CategoryDto;
import com.project.bizconnect.service.ProductService;
import com.project.bizconnect.service.CategoryService;
import com.project.bizconnect.service.AdminService;
import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.dto.RoleChangeRequest;
import com.project.bizconnect.entity.Role;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<String> getAdmin() {
        return ResponseEntity.ok("admin route");
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
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        CategoryDto created = categoryService.createCategory(categoryDto);
        return ResponseEntity.ok(created);
    }
}
