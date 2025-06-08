package com.project.bizconnect.controller;

import com.project.bizconnect.dto.MainOrderResponseDto;
import com.project.bizconnect.dto.OrderAddressUpdateDto;
import com.project.bizconnect.dto.OrderPaymentDto;
import com.project.bizconnect.dto.OrderRequestDto;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.MainOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customer/main-orders")
@RequiredArgsConstructor
public class MainOrderController {

    private final MainOrderService mainOrderService;

    @PostMapping
    public ResponseEntity<MainOrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal User customer) {
        MainOrderResponseDto createdOrder = mainOrderService.createOrder(orderRequestDto, customer);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{mainOrderId}")
    public ResponseEntity<MainOrderResponseDto> getMainOrder(
            @PathVariable Long mainOrderId,
            @AuthenticationPrincipal User customer) {
        MainOrderResponseDto order = mainOrderService.getMainOrder(mainOrderId, customer);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<MainOrderResponseDto>> getCustomerOrders(
            @AuthenticationPrincipal User customer) {
        List<MainOrderResponseDto> orders = mainOrderService.getCustomerMainOrders(customer);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{mainOrderId}/address")
    public ResponseEntity<MainOrderResponseDto> addAddressToOrder(
            @PathVariable Long mainOrderId,
            @Valid @RequestBody OrderAddressUpdateDto addressUpdateDto,
            @AuthenticationPrincipal User customer) {
        MainOrderResponseDto updatedOrder = mainOrderService.addAddressToMainOrder(mainOrderId, addressUpdateDto, customer);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{mainOrderId}/payment")
    public ResponseEntity<MainOrderResponseDto> processPayment(
            @PathVariable Long mainOrderId,
            @Valid @RequestBody OrderPaymentDto paymentDto,
            @AuthenticationPrincipal User customer) {
        MainOrderResponseDto updatedOrder = mainOrderService.processPayment(mainOrderId, paymentDto, customer);
        return ResponseEntity.ok(updatedOrder);
    }
}
