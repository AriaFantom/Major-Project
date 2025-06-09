package com.project.bizconnect.controller;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.CategoryService;
import com.project.bizconnect.service.FollowerService;
import com.project.bizconnect.service.OrderService;
import com.project.bizconnect.service.ProductService;
import com.project.bizconnect.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private final StoreService storeService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final FollowerService followerService;

    @PostMapping(value = "/stores", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<StoreDto> createStore(
            @RequestPart("store") StoreDto storeDto,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

        StoreDto created = storeService.createStoreWithImage(storeDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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

    @PostMapping(value = "/products", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductDto> createProduct(
            @RequestPart("product") ProductDto productDto,
            @RequestPart("images") List<MultipartFile> images) throws Exception {

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


        ProductDto created = productService.createProductWithImages(productDto, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getProductsByStore(@RequestParam Long storeId) {

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

        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(product.getStoreId());
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view products from stores you own");
        }

        return ResponseEntity.ok(product);
    }


    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDto>> getSellerOrders(@AuthenticationPrincipal User seller) {
        List<OrderResponseDto> orders = orderService.getSellerOrders(seller);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User seller) {
        OrderResponseDto order = orderService.getSellerOrder(orderId, seller);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{orderId}/status/shipped")
    public ResponseEntity<OrderResponseDto> markOrderAsShipped(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User seller) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, "SHIPPED", seller);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/orders/{orderId}/status/delivered")
    public ResponseEntity<OrderResponseDto> markOrderAsDelivered(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User seller) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, "DELIVERED", seller);
        return ResponseEntity.ok(updatedOrder);
    }

    // Store followers endpoints
    @GetMapping("/stores/{storeId}/followers")
    public ResponseEntity<List<FollowerDto>> getStoreFollowers(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User seller) {

        // Verify store ownership
        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(storeId);
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view followers for stores you own");
        }

        List<FollowerDto> followers = followerService.getStoreFollowers(storeId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/stores/{storeId}/followers/count")
    public ResponseEntity<Map<String, Long>> getStoreFollowerCount(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User seller) {

        // Verify store ownership
        boolean isOwner = storeService.isAuthenticatedUserStoreOwner(storeId);
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view follower count for stores you own");
        }

        long followerCount = followerService.getFollowerCount(storeId);
        return ResponseEntity.ok(Map.of("followerCount", followerCount));
    }
}

