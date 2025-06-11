package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.*;
import com.project.bizconnect.repository.AddressRepository;
import com.project.bizconnect.repository.OrderRepository;
import com.project.bizconnect.repository.ProductRepository;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.service.MainOrderService;
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
    private final MainOrderService mainOrderService;

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
        if (order.getCustomer().getId() != customer.getId()) {
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

        // Different rules for admins and sellers
        if (isAdmin) {
            // Admins can change to any status
            order.setStatus(newStatus);
        } else if (isSeller) {
            // Sellers are restricted to specific status transitions
            // Only allow SHIPPED and DELIVERED statuses to be set by seller
            if ((newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.DELIVERED) &&
                order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(newStatus);
            } else {
                throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
            }
        }

        Order updatedOrder = orderRepository.save(order);

        // After updating the sub-order status, update the main order status if this order is associated with a main order
        if (updatedOrder.getMainOrder() != null) {
            mainOrderService.updateMainOrderStatus(orderId);
        }

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

    @Override
    public StoreStatsDto getStoreStatistics(Long storeId, User seller) {
        // Validate store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        if (store.getOwner().getId() != seller.getId()) {
            throw new AccessDeniedException("You do not have access to this store");
        }

        // Find all orders for this store
        List<Order> storeOrders = orderRepository.findByStore(store);

        // Calculate total sales
        double totalSales = storeOrders.stream()
                .filter(order -> order.getStatus().toString().equals("PAID") ||
                                order.getStatus().toString().equals("SHIPPED") ||
                                order.getStatus().toString().equals("DELIVERED"))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Count total orders
        long totalOrders = storeOrders.size();

        // Count total products in the store
        long totalProducts = productRepository.countByStore(store);

        // Count unique customers who made orders
        long totalCustomers = storeOrders.stream()
                .map(order -> Long.valueOf(order.getCustomer().getId()))
                .distinct()
                .count();

        // Build and return the statistics DTO
        return StoreStatsDto.builder()
                .storeId(storeId)
                .storeName(store.getStoreName())
                .storeImageUrl(store.getImageUrl())
                .totalSales((long)totalSales)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .build();
    }

    @Override
    public List<OrderResponseDto> getRecentOrdersByStore(Long storeId, Integer limit, User seller) {
        // Validate store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        if (store.getOwner().getId() != seller.getId()) {
            throw new AccessDeniedException("You do not have access to this store");
        }

        // Find recent orders for this store with limit
        List<Order> recentOrders = orderRepository.findByStoreOrderByCreatedAtDesc(store,
                org.springframework.data.domain.PageRequest.of(0, limit));

        // Convert to DTOs and return
        return recentOrders.stream()
                .map(this::mapOrderToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopCustomerDto> getTopCustomersByStore(Long storeId, Integer limit, User seller) {
        // Validate store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));

        if (store.getOwner().getId() != seller.getId()) {
            throw new AccessDeniedException("You do not have access to this store");
        }

        // Find all orders for this store
        List<Order> storeOrders = orderRepository.findByStore(store);

        // Group orders by customer and calculate metrics
        Map<User, List<Order>> customerOrdersMap = storeOrders.stream()
                .filter(order -> order.getStatus().toString().equals("PAID") ||
                                order.getStatus().toString().equals("SHIPPED") ||
                                order.getStatus().toString().equals("DELIVERED"))
                .collect(Collectors.groupingBy(Order::getCustomer));

        // Create top customer DTOs
        List<TopCustomerDto> topCustomers = customerOrdersMap.entrySet().stream()
                .map(entry -> {
                    User customer = entry.getKey();
                    List<Order> customerOrders = entry.getValue();

                    double totalSpent = customerOrders.stream()
                            .mapToDouble(Order::getTotalAmount)
                            .sum();

                    return TopCustomerDto.builder()
                            .customerId(Long.valueOf(customer.getId()))
                            .customerName(customer.getFirstName() + " " + customer.getLastName())
                            .customerEmail(customer.getEmail())
                            .orderCount((long)customerOrders.size())
                            .totalSpent(totalSpent)
                            .build();
                })
                .sorted((c1, c2) -> Double.compare(c2.getTotalSpent(), c1.getTotalSpent())) // Sort by total spent descending
                .limit(limit)
                .collect(Collectors.toList());

        return topCustomers;
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
            itemDto.setProductId(item.getProduct().getProductId());
            itemDto.setProductName(item.getProduct().getName());
            // Since there is no imageUrl field, we'll set a placeholder or leave it out
            // itemDto.setProductImage(""); // Uncomment and modify if you have an alternative field
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPrice(item.getPrice());
            itemDto.setSubtotal(item.getPrice() * item.getQuantity());
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);

        return dto;
    }
}
