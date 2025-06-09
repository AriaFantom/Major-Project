package com.project.bizconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class StoreDto {
    private Long storeId;
    private String storeName;
    private String description;
    private boolean verified;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long ownerUserId;
    private String email;
    private String phoneNumber;
    private String address;
    private String websiteUrl;
    private String imageUrl;  // Added field for store image URL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Follower count for this store
    private Long followerCount;

    // Flag to indicate if the current user is following this store (used for UI)
    private Boolean currentUserFollowing;
}
