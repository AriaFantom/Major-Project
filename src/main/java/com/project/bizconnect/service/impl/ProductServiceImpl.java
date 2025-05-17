package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.entity.Product;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.Category;
import com.project.bizconnect.repository.ProductRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.repository.CategoryRespository;
import com.project.bizconnect.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRespository categoryRespository;

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setHasVariants(product.isHasVariants());
        dto.setStoreId(product.getStore().getStoreId());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
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
        product.setHasVariants(dto.isHasVariants());
        product.setStore(store);
        if (dto.getCategoryId() != null) {
            Category category = categoryRespository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }
}
