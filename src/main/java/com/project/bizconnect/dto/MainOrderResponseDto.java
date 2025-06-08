package com.project.bizconnect.dto;

import com.project.bizconnect.entity.OrderStatus;
import com.project.bizconnect.entity.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MainOrderResponseDto {
    private Long mainOrderId;
    private String orderReference;
    private Double totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long customerId;
    private String customerName;
    private AddressDto shippingAddress;
    private List<OrderResponseDto> subOrders = new ArrayList<>();
}
