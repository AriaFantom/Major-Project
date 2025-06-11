package com.project.bizconnect.service;

import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.ProductResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto getProductById(Long id);
    ProductDto createProduct(ProductDto productDto);
    List<ProductDto> getProductsByStoreId(Long storeId);

    // New methods for extended product details
    List<ProductResponseDto> getAllProductsWithDetails();
    ProductResponseDto getProductByIdWithDetails(Long id);
    List<ProductResponseDto> getProductsByStoreIdWithDetails(Long storeId);

    // New methods for handling product images
    ProductDto createProductWithImages(ProductDto productDto, List<MultipartFile> images) throws Exception;
    ProductDto addImageToProduct(Long productId, MultipartFile image) throws Exception;
    void deleteProductImage(Long productId, Long imageId);

    // New methods for best sellers and recently added products
    List<ProductResponseDto> getBestSellingProducts(Integer limit);
    List<ProductResponseDto> getRecentlyAddedProducts(Integer limit);
}
