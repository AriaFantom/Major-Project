package com.project.bizconnect.controller;

import com.project.bizconnect.dto.StoreDto;
import com.project.bizconnect.service.StoreService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/shop/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllVerifiedStores() {
        return ResponseEntity.ok(storeService.getAllVerifiedStores());
    }

    @GetMapping("/by/most-followed")
    public ResponseEntity<List<StoreDto>> getStoresByMostFollowers(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(storeService.getStoresByMostFollowers(limit));
    }

    @GetMapping("/image/{objectName}")
    public ResponseEntity<InputStreamResource> getStoreImage(@PathVariable String objectName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

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
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDto> getVerifiedStoreById(@PathVariable Long storeId) {
        return storeService.getVerifiedStoreById(storeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
