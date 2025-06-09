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
    private String imageUrl; // Added image URL field
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User details
    private int userId;
    private String userFirstName; // Separate first name
    private String userLastName; // Separate last name
    private String userName; // Keep for backward compatibility
}
