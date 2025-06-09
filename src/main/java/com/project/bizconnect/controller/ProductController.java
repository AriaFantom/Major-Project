package com.project.bizconnect.controller;

import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.ProductResponseDto;
import com.project.bizconnect.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/shop/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/details")
    public ResponseEntity<List<ProductResponseDto>> getAllProductsWithDetails() {
        return ResponseEntity.ok(productService.getAllProductsWithDetails());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ProductResponseDto> getProductByIdWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductByIdWithDetails(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.createProduct(productDto));
    }

    @PostMapping(value = "/with-images", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductDto> createProductWithImages(
            @RequestPart("product") ProductDto productDto,
            @RequestPart("images") List<MultipartFile> images) throws Exception {
        return ResponseEntity.ok(productService.createProductWithImages(productDto, images));
    }

    @PostMapping(value = "/{productId}/images", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductDto> addImageToProduct(
            @PathVariable Long productId,
            @RequestPart("image") MultipartFile image) throws Exception {
        return ResponseEntity.ok(productService.addImageToProduct(productId, image));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductDto>> getProductsByStoreId(@PathVariable Long storeId) {
        return ResponseEntity.ok(productService.getProductsByStoreId(storeId));
    }

    @GetMapping("/store/{storeId}/details")
    public ResponseEntity<List<ProductResponseDto>> getProductsByStoreIdWithDetails(@PathVariable Long storeId) {
        return ResponseEntity.ok(productService.getProductsByStoreIdWithDetails(storeId));
    }
}
