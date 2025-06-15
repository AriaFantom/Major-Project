package com.project.bizconnect.service;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    List<OrderResponseDto> createOrder(OrderRequestDto orderRequestDto, User customer);
    OrderResponseDto getOrder(Long orderId, User customer);
    OrderResponseDto addAddressToOrder(Long orderId, OrderAddressUpdateDto addressUpdateDto, User customer);
    OrderResponseDto processPayment(Long orderId, OrderPaymentDto paymentDto, User customer);
    List<OrderResponseDto> getCustomerOrders(User customer);
    OrderResponseDto updateOrderStatus(Long orderId, String status, User user);

    List<OrderResponseDto> getSellerOrders(User seller);
    OrderResponseDto getSellerOrder(Long orderId, User seller);

    StoreStatsDto getStoreStatistics(Long storeId, User seller);
    List<OrderResponseDto> getRecentOrdersByStore(Long storeId, Integer limit, User seller);
    List<TopCustomerDto> getTopCustomersByStore(Long storeId, Integer limit, User seller);
    List<CustomerStatsDto> getAllCustomersByStore(Long storeId, User seller);

    List<OrderStatisticsDto> getDailyOrderStatisticsByStore(Long storeId, LocalDate startDate, LocalDate endDate, User seller);
}
