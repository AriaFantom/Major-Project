package com.project.bizconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SellerStatsDto {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime joiningDate;
    private List<SellerStoreDto> stores;

    @Data
    public static class SellerStoreDto {
        private Long storeId;
        private String storeName;
        private String imageUrl;  // Added field for store image URL
    }
}
