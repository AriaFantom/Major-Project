package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPerformanceDto {
    private String name;           // Store name
    private BigDecimal sales;      // Total sales amount
    private Long customers;        // Total number of customers who ordered
}
