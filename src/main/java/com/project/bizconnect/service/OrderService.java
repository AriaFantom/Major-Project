package com.project.bizconnect.service;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.User;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> createOrder(OrderRequestDto orderRequestDto, User customer);
    OrderResponseDto getOrder(Long orderId, User customer);
    OrderResponseDto addAddressToOrder(Long orderId, OrderAddressUpdateDto addressUpdateDto, User customer);
    OrderResponseDto processPayment(Long orderId, OrderPaymentDto paymentDto, User customer);
    List<OrderResponseDto> getCustomerOrders(User customer);
    OrderResponseDto updateOrderStatus(Long orderId, String status, User user);

    // Seller specific methods
    List<OrderResponseDto> getSellerOrders(User seller);
    OrderResponseDto getSellerOrder(Long orderId, User seller);
}
