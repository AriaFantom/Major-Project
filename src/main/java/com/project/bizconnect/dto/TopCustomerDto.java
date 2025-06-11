package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDto {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long orderCount;
    private Double totalSpent;
}
