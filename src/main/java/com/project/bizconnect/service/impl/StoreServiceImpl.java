package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
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
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
