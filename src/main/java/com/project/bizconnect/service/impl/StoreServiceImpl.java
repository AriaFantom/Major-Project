package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.CategoryResponseDto;
import com.project.bizconnect.dto.ProductResponseDto;
import com.project.bizconnect.dto.SearchResultDto;
import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.entity.Category;
import com.project.bizconnect.entity.Product;
import com.project.bizconnect.entity.ProductImage;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.CategoryRepository;
import com.project.bizconnect.repository.ProductRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.StoreService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final MinioClient minioClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;  // Add CategoryRepository

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private StoreDto mapToDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setDescription(store.getDescription());
        dto.setVerified(store.isVerified());
        dto.setOwnerUserId(Long.valueOf(store.getOwner().getId()));
        dto.setEmail(store.getEmail());
        dto.setPhoneNumber(store.getPhoneNumber());
        dto.setAddress(store.getAddress());
        dto.setWebsiteUrl(store.getWebsiteUrl());

        // Generate full URL for store image if available
        if (store.getImageUrl() != null && !store.getImageUrl().isEmpty()) {
            dto.setImageUrl(org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/shop/stores/image/")
                    .path(store.getImageUrl())
                    .toUriString());
        } else {
            dto.setImageUrl(null);
        }

        // Set totalProducts
        dto.setTotalProducts((long) productRepository.findByStore(store).size());
        // Set followerCount using followers list size
        dto.setFollowerCount((long) store.getFollowers().size());

        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        return dto;
    }

    @Override
    public StoreDto createStore(StoreDto storeDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Unauthorized");
        }
        User user = (User) principal;
        Store store = new Store();
        store.setStoreName(storeDto.getStoreName());
        store.setDescription(storeDto.getDescription());
        store.setOwner(user);
        store.setEmail(storeDto.getEmail());
        store.setPhoneNumber(storeDto.getPhoneNumber());
        store.setAddress(storeDto.getAddress());
        store.setWebsiteUrl(storeDto.getWebsiteUrl());
        Store saved = storeRepository.save(store);
        return mapToDto(saved);
    }

    @Override
    public StoreDto createStoreWithImage(StoreDto storeDto, MultipartFile image) throws Exception {
        log.info("Creating store with image. Store name: {}, Image null: {}",
                storeDto.getStoreName(), image == null);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Unauthorized");
        }

        User user = (User) principal;
        Store store = new Store();
        store.setStoreName(storeDto.getStoreName());
        store.setDescription(storeDto.getDescription());
        store.setOwner(user);
        store.setEmail(storeDto.getEmail());
        store.setPhoneNumber(storeDto.getPhoneNumber());
        store.setAddress(storeDto.getAddress());
        store.setWebsiteUrl(storeDto.getWebsiteUrl());

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            log.info("Processing image upload. Original filename: {}, Content type: {}, Size: {}",
                    image.getOriginalFilename(), image.getContentType(), image.getSize());

            // Generate a unique file name
            String originalFilename = image.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = "store-" + UUID.randomUUID() + extension;
            log.info("Generated object name for MinIO: {}", objectName);

            // Upload to MinIO
            try (InputStream inputStream = image.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, image.getSize(), -1)
                                .contentType(image.getContentType())
                                .build()
                );

                // Set the image URL
                log.info("Successfully uploaded to MinIO. Setting imageUrl: {}", objectName);
                store.setImageUrl(objectName);
            } catch (Exception e) {
                log.error("Error uploading image to MinIO", e);
                throw e;
            }
        } else {
            log.info("No image provided or image is empty");
        }

        Store saved = storeRepository.save(store);
        log.info("Store saved to database. Store ID: {}, Has imageUrl: {}",
                saved.getStoreId(), saved.getImageUrl() != null);

        return mapToDto(saved);
    }

    @Override
    public List<StoreDto> getStoresByAuthenticatedSeller() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Unauthorized");
        }

        User user = (User) principal;

        // Check if the user has the SELLER role
        if (user.getRole() != Role.SELLER) {
            throw new IllegalStateException("Only sellers can access their stores");
        }

        List<Store> stores = storeRepository.findByOwner(user);

        return stores.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAuthenticatedUserStoreOwner(Long storeId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }

        User user = (User) principal;
        Optional<Store> storeOptional = storeRepository.findById(storeId);

        if (storeOptional.isEmpty()) {
            return false;
        }

        Store store = storeOptional.get();
        return store.getOwner().getId() == user.getId();
    }

    @Override
    public List<StoreDto> getAllVerifiedStores() {
        return storeRepository.findByIsVerifiedTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreDto> getStoresByIds(List<Long> storeIds, User currentUser) {
        List<Store> stores = storeRepository.findAllById(storeIds);
        return stores.stream()
                .map(store -> {
                    StoreDto dto = mapToDto(store);
                    // Add follower count
                    dto.setFollowerCount((long) store.getFollowers().size());

                    // Check if current user is following
                    if (currentUser != null) {
                        boolean isFollowing = store.getFollowers().stream()
                                .anyMatch(follower -> follower.getUser().getId() == currentUser.getId());
                        dto.setCurrentUserFollowing(isFollowing);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreDto> getAllStoresWithFollowerCounts() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(store -> {
                    StoreDto dto = mapToDto(store);
                    // Add follower count
                    dto.setFollowerCount((long) store.getFollowers().size());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StoreDto> getVerifiedStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .filter(Store::isVerified) // Only return the store if it's verified
                .map(store -> {
                    StoreDto dto = mapToDto(store);
                    // Add follower count
                    dto.setFollowerCount((long) store.getFollowers().size());
                    return dto;
                });
    }

    @Override
    public List<StoreDto> getStoresByMostFollowers(Integer limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return storeRepository.findAllByOrderByFollowersCountDesc(pageable).stream()
                .filter(Store::isVerified) // Only return verified stores
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SearchResultDto searchProductsAndCategories(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new SearchResultDto(List.of(), List.of());
        }

        // Search for products by name (case-insensitive, partial match)
        List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTerm.trim());

        // Search for categories by name (case-insensitive, partial match)
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(searchTerm.trim());

        // Map products to DTOs with detailed information
        List<ProductResponseDto> productDtos = products.stream()
                .map(this::mapToProductResponseDto)
                .collect(Collectors.toList());

        // Map categories to DTOs with detailed information
        List<CategoryResponseDto> categoryDtos = categories.stream()
                .map(this::mapToCategoryResponseDto)
                .collect(Collectors.toList());

        // Return combined results
        return new SearchResultDto(productDtos, categoryDtos);
    }

    private ProductResponseDto mapToProductResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());

        // Set store information
        Store store = product.getStore();
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());

        // Set category information if available
        Category category = product.getCategory();
        if (category != null) {
            dto.setCategoryId(category.getId());
            dto.setCategoryName(category.getName());
        }

        // Add image URLs
        if (product.getImages() != null) {
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);
        }

        return dto;
    }

    private CategoryResponseDto mapToCategoryResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());

        // Set store info
        if (category.getStore() != null) {
            dto.setStoreId(category.getStore().getStoreId());
            dto.setStoreName(category.getStore().getStoreName());
        }

        // Count products in this category
        if (category.getProducts() != null) {
            dto.setProductCount(category.getProducts().size());
        } else {
            dto.setProductCount(0);
        }

        return dto;
    }
}
