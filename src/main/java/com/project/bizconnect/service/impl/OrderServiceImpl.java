package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.*;
import com.project.bizconnect.repository.AddressRepository;
import com.project.bizconnect.repository.OrderRepository;
import com.project.bizconnect.repository.ProductRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public List<OrderResponseDto> createOrder(OrderRequestDto orderRequestDto, User customer) {
        Map<Long, List<OrderItemRequestDto>> storeToItemsMap = new HashMap<>();
        Map<Long, Store> storeMap = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();

        // First pass: Load all products and group by store
        for (OrderItemRequestDto itemDto : orderRequestDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + itemDto.getProductId()));

            Store store = product.getStore();
            Long storeId = store.getStoreId();

            // Add to maps for later reference
            productMap.put(product.getProductId(), product);
            storeMap.putIfAbsent(storeId, store);

            // Group order items by store
            storeToItemsMap.computeIfAbsent(storeId, k -> new ArrayList<>()).add(itemDto);
        }

        List<OrderResponseDto> createdOrders = new ArrayList<>();

        // Second pass: Create individual orders for each store
        for (Map.Entry<Long, List<OrderItemRequestDto>> entry : storeToItemsMap.entrySet()) {
            Long storeId = entry.getKey();
            List<OrderItemRequestDto> storeItems = entry.getValue();
            Store store = storeMap.get(storeId);

            // Create a new order for this store
            Order order = new Order();
            order.setCustomer(customer);
            order.setStore(store);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.UNPAID);

            // Calculate total amount and create order items
            double totalAmount = 0.0;
            List<OrderItem> orderItems = new ArrayList<>();

            for (OrderItemRequestDto itemDto : storeItems) {
                Product product = productMap.get(itemDto.getProductId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(product.getPrice());

                totalAmount += product.getPrice() * itemDto.getQuantity();
                orderItems.add(orderItem);
            }

            order.setTotalAmount(totalAmount);
            order.setItems(orderItems);

            // Save the order
            Order savedOrder = orderRepository.save(order);
            createdOrders.add(mapOrderToResponseDto(savedOrder));
        }

        return createdOrders;
    }

    @Override
    public OrderResponseDto getOrder(Long orderId, User customer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        // Ensure the order belongs to the customer
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }

        return mapOrderToResponseDto(order);
    }

    @Override
    @Transactional
    public OrderResponseDto addAddressToOrder(Long orderId, OrderAddressUpdateDto addressUpdateDto, User customer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        // Ensure the order belongs to the customer
        if (order.getCustomer().getId() != customer.getId()) {
            throw new AccessDeniedException("You don't have permission to update this order");
        }

        // Get the address
        Address address = addressRepository.findByIdAndUser(addressUpdateDto.getAddressId(), customer)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressUpdateDto.getAddressId()));

        // Update the order with address
        order.setShippingAddress(address);

        Order updatedOrder = orderRepository.save(order);
        return mapOrderToResponseDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto processPayment(Long orderId, OrderPaymentDto paymentDto, User customer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        // Ensure the order belongs to the customer
        if (order.getCustomer().getId() != customer.getId()) {
            throw new AccessDeniedException("You don't have permission to update this order");
        }

        // Ensure order is in PENDING status and address is set
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        if (order.getShippingAddress() == null) {
            throw new IllegalStateException("Shipping address must be set before processing payment");
        }

        // Update the payment information
        order.setPaymentMethod(paymentDto.getPaymentMethod());
        order.setPaymentId(paymentDto.getPaymentId());
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.PROCESSING);

        Order updatedOrder = orderRepository.save(order);
        return mapOrderToResponseDto(updatedOrder);
    }

    @Override
    public List<OrderResponseDto> getCustomerOrders(User customer) {
        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        return orders.stream()
                .map(this::mapOrderToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, String status, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        // Check if user is admin or the seller of the store
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSeller = user.getRole() == Role.SELLER && order.getStore().getOwner().getId() == user.getId();

        if (!isAdmin && !isSeller) {
            throw new AccessDeniedException("You don't have permission to update this order status");
        }

        // Validate the status transition
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        // Only allow SHIPPED and DELIVERED statuses to be set by seller or admin
        if ((newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.DELIVERED) &&
            order.getStatus() == OrderStatus.PROCESSING) {
            order.setStatus(newStatus);
        } else {
            throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        Order updatedOrder = orderRepository.save(order);
        return mapOrderToResponseDto(updatedOrder);
    }

    @Override
    public List<OrderResponseDto> getSellerOrders(User seller) {
        // Validate seller role
        if (seller.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Only sellers can access their orders");
        }

        // Get all orders for stores owned by this seller
        List<Order> orders = orderRepository.findByStore_Owner(seller);
        return orders.stream()
                .map(this::mapOrderToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getSellerOrder(Long orderId, User seller) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        // Verify seller owns the store this order belongs to
        if (seller.getId() != order.getStore().getOwner().getId()) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }

        return mapOrderToResponseDto(order);
    }

    private OrderResponseDto mapOrderToResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Set customer information
        dto.setCustomerId((long) order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());

        // Set store information
        dto.setStoreId(order.getStore().getStoreId());
        dto.setStoreName(order.getStore().getStoreName());

        // Set shipping address if available
        if (order.getShippingAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setId(order.getShippingAddress().getId());
            addressDto.setFullName(order.getShippingAddress().getFullName());
            addressDto.setStreetAddress(order.getShippingAddress().getStreetAddress());
            addressDto.setCity(order.getShippingAddress().getCity());
            addressDto.setState(order.getShippingAddress().getState());
            addressDto.setZipCode(order.getShippingAddress().getZipCode());
            addressDto.setCountry(order.getShippingAddress().getCountry());
            dto.setShippingAddress(addressDto);
        }

        // Set order items
        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setOrderItemId(item.getOrderItemId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setProductImage(item.getProduct().getImageUrl());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPrice(item.getPrice());
            itemDto.setSubtotal(item.getPrice() * item.getQuantity());
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);

        return dto;
    }
}
