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
public class CategorySalesPercentageDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal salesPercentage;
    private BigDecimal totalSalesAmount;
}
