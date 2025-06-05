package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreWithUserDto {
    // Store details
    private Long storeId;
    private String storeName;
    private String description;
    private boolean verified;
    private String email;
    private String phoneNumber;
    private String address;
    private String websiteUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User details
    private int userId;
    private String userName;
}
