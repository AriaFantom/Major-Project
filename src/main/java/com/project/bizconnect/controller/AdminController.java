package com.project.bizconnect.controller;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AdminService adminService;
    private final StoreService storeService;
    private final OrderService orderService;
    private final MainOrderService mainOrderService;
    private final FollowerService followerService;

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

    @PostMapping(value = "/products", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestPart("product") ProductDto productDto,
            @RequestPart("images") List<MultipartFile> images) throws Exception {

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

        // Use the new method that supports image uploads
        ProductDto created = productService.createProductWithImages(productDto, images);

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

    @GetMapping("/orders")
    public ResponseEntity<List<MainOrderResponseDto>> getAllOrders() {
        List<MainOrderResponseDto> orders = adminService.getAllOrdersWithDetails();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{mainOrderId}")
    public ResponseEntity<MainOrderResponseDto> getOrderById(@PathVariable Long mainOrderId) {
        MainOrderResponseDto order = adminService.getOrderByIdWithDetails(mainOrderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<MainOrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId, @RequestBody OrderStatusUpdateDto statusUpdate) {
        MainOrderResponseDto updatedOrder = adminService.updateOrderStatus(orderId, statusUpdate);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/suborders/{subOrderId}/status")
    public ResponseEntity<SubOrderDto> updateSubOrderStatus(
            @PathVariable Long subOrderId, @RequestBody SubOrderStatusUpdateDto statusUpdate) {
        SubOrderDto updatedSubOrder = adminService.updateSubOrderStatus(subOrderId, statusUpdate);
        return ResponseEntity.ok(updatedSubOrder);
    }

    @PatchMapping("/main-order/{id}")
    public ResponseEntity<MainOrderResponseDto> updateMainOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateDto statusUpdateDto,
            @AuthenticationPrincipal User admin) {
        // Verify that the user is an admin
        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update order status");
        }

        MainOrderResponseDto updatedOrder = mainOrderService.updateMainOrderStatus(id, statusUpdateDto.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/order/{id}")
    public ResponseEntity<OrderResponseDto> updateSubOrderStatus(
            @PathVariable Long id,
            @RequestBody SubOrderStatusUpdateDto statusUpdateDto,
            @AuthenticationPrincipal User admin) {
        // Verify that the user is an admin
        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update order status");
        }

        OrderResponseDto updatedOrder = orderService.updateOrderStatus(id, statusUpdateDto.getStatus(), admin);
        return ResponseEntity.ok(updatedOrder);
    }

    // Store followers endpoints for admin
    @GetMapping("/stores/{storeId}/followers")
    public ResponseEntity<List<FollowerDto>> getStoreFollowers(@PathVariable Long storeId) {
        List<FollowerDto> followers = followerService.getStoreFollowers(storeId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/stores/{storeId}/followers/count")
    public ResponseEntity<Map<String, Long>> getStoreFollowerCount(@PathVariable Long storeId) {
        long followerCount = followerService.getFollowerCount(storeId);
        return ResponseEntity.ok(Map.of("followerCount", followerCount));
    }

    // Get all stores with follower counts
    @GetMapping("/stores/with-followers")
    public ResponseEntity<List<StoreDto>> getAllStoresWithFollowerCounts() {
        List<StoreDto> stores = storeService.getAllStoresWithFollowerCounts();
        return ResponseEntity.ok(stores);
    }
}
