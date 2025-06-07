package com.project.bizconnect.dto;

import lombok.Data;

@Data
public class OrderItemResponseDto {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}
