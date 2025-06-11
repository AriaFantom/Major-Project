package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.ProductResponseDto;
import com.project.bizconnect.entity.Product;
import com.project.bizconnect.entity.ProductImage;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.Category;
import com.project.bizconnect.repository.ProductRepository;
import com.project.bizconnect.repository.ProductImageRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.repository.CategoryRespository;
import com.project.bizconnect.service.FileStorageService;
import com.project.bizconnect.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRespository categoryRespository;
    private final FileStorageService fileStorageService;
    private final ProductImageRepository productImageRepository;

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setStoreId(product.getStore().getStoreId());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);

        // Add image URLs
        if (product.getImages() != null) {
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);
        }

        return dto;
    }

    // Add new method to map to ProductResponseDto with additional details
    private ProductResponseDto mapToResponseDto(Product product) {
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

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapToDto(product);
    }

    @Override
    public ProductDto createProduct(ProductDto dto) {
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setStore(store);

        if (dto.getCategoryId() != null) {
            Category category = categoryRespository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ProductDto createProductWithImages(ProductDto dto, List<MultipartFile> images) throws Exception {
        // First create the product
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setStore(store);

        if (dto.getCategoryId() != null) {
            Category category = categoryRespository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        // Save the product first to get an ID
        Product savedProduct = productRepository.save(product);

        // Now process and save images if provided
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    // Upload the image to MinIO
                    String imageUrl = fileStorageService.uploadFile(image);

                    // Create product image entity and associate with product
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imageUrl);
                    productImage.setProduct(savedProduct);

                    // Add to the product's images list
                    savedProduct.getImages().add(productImage);
                }
            }

            // Save the product again with images
            savedProduct = productRepository.save(savedProduct);
        }

        return mapToDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto addImageToProduct(Long productId, MultipartFile image) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (image != null && !image.isEmpty()) {
            // Upload the image to MinIO
            String imageUrl = fileStorageService.uploadFile(image);

            // Create product image entity and associate with product
            ProductImage productImage = new ProductImage();
            productImage.setImageUrl(imageUrl);
            productImage.setProduct(product);

            // Add to the product's images list
            product.getImages().add(productImage);

            // Save the product with the new image
            product = productRepository.save(product);
        }

        return mapToDto(product);
    }

    @Override
    @Transactional
    public void deleteProductImage(Long productId, Long imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductImage imageToRemove = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Product image not found"));

        // Verify the image belongs to this product
        if (!imageToRemove.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }

        // Remove from product's collection
        product.getImages().remove(imageToRemove);

        // Delete the image
        productImageRepository.delete(imageToRemove);

        // Save the product
        productRepository.save(product);
    }

    @Override
    public List<ProductDto> getProductsByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        return productRepository.findByStore(store).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getAllProductsWithDetails() {
        return productRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto getProductByIdWithDetails(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapToResponseDto(product);
    }

    @Override
    public List<ProductResponseDto> getProductsByStoreIdWithDetails(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        return productRepository.findByStore(store).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getBestSellingProducts(Integer limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return productRepository.findBestSellingProducts(pageable).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getRecentlyAddedProducts(Integer limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return productRepository.findByOrderByCreatedAtDesc(pageable).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
}
