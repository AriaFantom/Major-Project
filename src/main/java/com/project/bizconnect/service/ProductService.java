package com.project.bizconnect.service;

import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.ProductResponseDto;
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
}
