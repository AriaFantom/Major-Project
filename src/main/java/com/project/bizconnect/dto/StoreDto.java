package com.project.bizconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoreDto {
    private Long storeId;
    private String storeName;
    private String description;
    private boolean verified;
    private Long ownerUserId;
    private String email;
    private String phoneNumber;
    private String address;
    private String websiteUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
