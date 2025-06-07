package com.project.bizconnect.dto;

import com.project.bizconnect.entity.OrderStatus;
import com.project.bizconnect.entity.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long orderId;
    private OrderStatus status;
    private Double totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long customerId;
    private String customerName;
    private Long storeId;
    private String storeName;
    private AddressDto shippingAddress;
    private List<OrderItemResponseDto> items;
}
