package com.project.bizconnect.controller;

import com.project.bizconnect.dto.OrderResponseDto;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getSellerOrders(@AuthenticationPrincipal User seller) {
        List<OrderResponseDto> orders = orderService.getSellerOrders(seller);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User seller) {
        OrderResponseDto order = orderService.getSellerOrder(orderId, seller);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}/status/shipped")
    public ResponseEntity<OrderResponseDto> markOrderAsShipped(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User seller) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, "SHIPPED", seller);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{orderId}/status/delivered")
    public ResponseEntity<OrderResponseDto> markOrderAsDelivered(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User seller) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, "DELIVERED", seller);
        return ResponseEntity.ok(updatedOrder);
    }
}
