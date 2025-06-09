package com.project.bizconnect.service.impl;
import com.project.bizconnect.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String endpoint;

    public FileStorageServiceImpl(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucketName,
            @Value("${minio.endpoint}") String endpoint
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.endpoint = endpoint;
    }

    @PostConstruct
    public void initBucket() throws Exception {
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while checking or creating bucket", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String objectName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        // Build the full URL using the ProductController endpoint for images
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/shop/products/images/")
                .path(objectName)
                .toUriString();
    }
}
