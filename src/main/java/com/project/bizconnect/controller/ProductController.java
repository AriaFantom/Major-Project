package com.project.bizconnect.controller;

import com.project.bizconnect.dto.ProductDto;
import com.project.bizconnect.dto.ProductResponseDto;
import com.project.bizconnect.service.ProductService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/shop/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

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

    @GetMapping("/images/{objectName}")
    public ResponseEntity<InputStreamResource> getProductImage(@PathVariable String objectName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            // Try to determine the content type
            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            if (objectName.toLowerCase().endsWith(".jpg") || objectName.toLowerCase().endsWith(".jpeg")) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            } else if (objectName.toLowerCase().endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
