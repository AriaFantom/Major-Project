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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
