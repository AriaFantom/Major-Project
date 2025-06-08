package com.project.bizconnect.controller;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.OrderService;
import com.project.bizconnect.service.MainOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MainOrderService mainOrderService;

    // Customer order endpoints
    @RestController
    @RequestMapping("/api/customer/orders")
    @RequiredArgsConstructor
    public class CustomerOrderController {

        private final OrderService orderService;

        @PostMapping
        public ResponseEntity<List<OrderResponseDto>> createOrder(
                @Valid @RequestBody OrderRequestDto orderRequestDto,
                @AuthenticationPrincipal User customer) {
            List<OrderResponseDto> createdOrders = orderService.createOrder(orderRequestDto, customer);
            return new ResponseEntity<>(createdOrders, HttpStatus.CREATED);
        }

        @GetMapping("/{orderId}")
        public ResponseEntity<OrderResponseDto> getOrder(
                @PathVariable Long orderId,
                @AuthenticationPrincipal User customer) {
            OrderResponseDto order = orderService.getOrder(orderId, customer);
            return ResponseEntity.ok(order);
        }

        @GetMapping
        public ResponseEntity<List<OrderResponseDto>> getCustomerOrders(
                @AuthenticationPrincipal User customer) {
            List<OrderResponseDto> orders = orderService.getCustomerOrders(customer);
            return ResponseEntity.ok(orders);
        }

        @PatchMapping("/{orderId}/address")
        public ResponseEntity<OrderResponseDto> addAddressToOrder(
                @PathVariable Long orderId,
                @Valid @RequestBody OrderAddressUpdateDto addressUpdateDto,
                @AuthenticationPrincipal User customer) {
            OrderResponseDto updatedOrder = orderService.addAddressToOrder(orderId, addressUpdateDto, customer);
            return ResponseEntity.ok(updatedOrder);
        }

        @PatchMapping("/{orderId}/payment")
        public ResponseEntity<OrderResponseDto> processPayment(
                @PathVariable Long orderId,
                @Valid @RequestBody OrderPaymentDto paymentDto,
                @AuthenticationPrincipal User customer) {
            OrderResponseDto updatedOrder = orderService.processPayment(orderId, paymentDto, customer);
            return ResponseEntity.ok(updatedOrder);
        }
    }
}
