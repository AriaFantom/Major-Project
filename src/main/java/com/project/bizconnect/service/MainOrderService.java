package com.project.bizconnect.service;

import com.project.bizconnect.dto.MainOrderResponseDto;
import com.project.bizconnect.dto.OrderAddressUpdateDto;
import com.project.bizconnect.dto.OrderPaymentDto;
import com.project.bizconnect.dto.OrderRequestDto;
import com.project.bizconnect.entity.User;

import java.util.List;

public interface MainOrderService {
    MainOrderResponseDto createOrder(OrderRequestDto orderRequestDto, User customer);
    MainOrderResponseDto getMainOrder(Long mainOrderId, User customer);
    List<MainOrderResponseDto> getCustomerMainOrders(User customer);
    MainOrderResponseDto addAddressToMainOrder(Long mainOrderId, OrderAddressUpdateDto addressUpdateDto, User customer);
    MainOrderResponseDto processPayment(Long mainOrderId, OrderPaymentDto paymentDto, User customer);
}
