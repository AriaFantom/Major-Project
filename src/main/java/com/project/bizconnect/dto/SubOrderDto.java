package com.project.bizconnect.dto;

import com.project.bizconnect.entity.OrderStatus;
import com.project.bizconnect.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubOrderDto {
    private Long orderId;
    private OrderStatus status;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private Long storeId;
    private String storeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponseDto> items;
}
