package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatsDto {
    private Long storeId;
    private String storeName;
    private String storeImageUrl;
    private Long totalSales; // Total monetary value of all sales
    private Long totalOrders; // Count of orders
    private Long totalProducts; // Count of products
    private Long totalCustomers; // Count of unique customers
}
