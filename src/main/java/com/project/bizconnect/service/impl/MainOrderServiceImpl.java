package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.*;
import com.project.bizconnect.repository.*;
import com.project.bizconnect.service.MainOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainOrderServiceImpl implements MainOrderService {

    private final MainOrderRepository mainOrderRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public MainOrderResponseDto createOrder(OrderRequestDto orderRequestDto, User customer) {
        // Create a main order
        MainOrder mainOrder = new MainOrder();
        mainOrder.setCustomer(customer);
        mainOrder.setOrderReference(generateOrderReference());
        mainOrder.setStatus(OrderStatus.PENDING);
        mainOrder.setPaymentStatus(PaymentStatus.UNPAID);
        mainOrder.setTotalAmount(0.0); // Initialize with 0.0 to avoid NULL constraint violation

        // Group products by store
        Map<Long, List<OrderItemRequestDto>> storeToItemsMap = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();
        Map<Long, Store> storeMap = new HashMap<>();

        double totalMainOrderAmount = 0.0;

        // First pass: Load all products and organize by store
        for (OrderItemRequestDto itemDto : orderRequestDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + itemDto.getProductId()));

            Store store = product.getStore();
            Long storeId = store.getStoreId();

            productMap.put(product.getProductId(), product);
            storeMap.putIfAbsent(storeId, store);

            storeToItemsMap.computeIfAbsent(storeId, k -> new ArrayList<>()).add(itemDto);
        }

        // Save the main order first
        MainOrder savedMainOrder = mainOrderRepository.save(mainOrder);

        // Second pass: Create individual orders for each store
        List<Order> subOrders = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItemRequestDto>> entry : storeToItemsMap.entrySet()) {
            Long storeId = entry.getKey();
            List<OrderItemRequestDto> storeItems = entry.getValue();
            Store store = storeMap.get(storeId);

            // Create a sub-order for this store
            Order order = new Order();
            order.setCustomer(customer);
            order.setStore(store);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setMainOrder(savedMainOrder);

            // Calculate total amount and create order items
            double totalSubOrderAmount = 0.0;
            List<OrderItem> orderItems = new ArrayList<>();

            for (OrderItemRequestDto itemDto : storeItems) {
                Product product = productMap.get(itemDto.getProductId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(product.getPrice());

                double itemTotal = product.getPrice() * itemDto.getQuantity();
                totalSubOrderAmount += itemTotal;
                orderItems.add(orderItem);
            }

            order.setTotalAmount(totalSubOrderAmount);
            order.setItems(orderItems);

            Order savedOrder = orderRepository.save(order);
            subOrders.add(savedOrder);

            totalMainOrderAmount += totalSubOrderAmount;
        }

        // Update main order with total amount and sub-orders
        savedMainOrder.setTotalAmount(totalMainOrderAmount);
        savedMainOrder.setSubOrders(subOrders);
        savedMainOrder = mainOrderRepository.save(savedMainOrder);

        // Map to DTO and return
        return mapMainOrderToDto(savedMainOrder);
    }

    @Override
    public MainOrderResponseDto getMainOrder(Long mainOrderId, User customer) {
        MainOrder mainOrder = mainOrderRepository.findById(mainOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Main order not found with id: " + mainOrderId));

        if (mainOrder.getCustomer().getId() != customer.getId()) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }

        return mapMainOrderToDto(mainOrder);
    }

    @Override
    public List<MainOrderResponseDto> getCustomerMainOrders(User customer) {
        List<MainOrder> mainOrders = mainOrderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        return mainOrders.stream()
                .map(this::mapMainOrderToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MainOrderResponseDto addAddressToMainOrder(Long mainOrderId, OrderAddressUpdateDto addressUpdateDto, User customer) {
        MainOrder mainOrder = mainOrderRepository.findById(mainOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Main order not found with id: " + mainOrderId));

        if (mainOrder.getCustomer().getId() != customer.getId()) {
            throw new AccessDeniedException("You don't have permission to update this order");
        }

        if (mainOrder.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        // Get the address
        Address address = addressRepository.findByIdAndUser(addressUpdateDto.getAddressId(), customer)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressUpdateDto.getAddressId()));

        // Update the main order and all sub-orders with the address
        mainOrder.setShippingAddress(address);

        for (Order subOrder : mainOrder.getSubOrders()) {
            subOrder.setShippingAddress(address);
            orderRepository.save(subOrder);
        }

        MainOrder updatedMainOrder = mainOrderRepository.save(mainOrder);
        return mapMainOrderToDto(updatedMainOrder);
    }

    @Override
    @Transactional
    public MainOrderResponseDto processPayment(Long mainOrderId, OrderPaymentDto paymentDto, User customer) {
        MainOrder mainOrder = mainOrderRepository.findById(mainOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Main order not found with id: " + mainOrderId));

        if (mainOrder.getCustomer().getId() != customer.getId()) {
            throw new AccessDeniedException("You don't have permission to update this order");
        }

        if (mainOrder.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        if (mainOrder.getShippingAddress() == null) {
            throw new IllegalStateException("Shipping address must be set before processing payment");
        }

        // Update the main order payment details
        mainOrder.setPaymentMethod(paymentDto.getPaymentMethod());
        mainOrder.setPaymentId(paymentDto.getPaymentId());
        mainOrder.setPaymentStatus(PaymentStatus.PAID);
        mainOrder.setStatus(OrderStatus.PROCESSING);

        // Update all sub-orders
        for (Order subOrder : mainOrder.getSubOrders()) {
            subOrder.setPaymentMethod(paymentDto.getPaymentMethod());
            subOrder.setPaymentId(paymentDto.getPaymentId());
            subOrder.setPaymentStatus(PaymentStatus.PAID);
            subOrder.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(subOrder);
        }

        MainOrder updatedMainOrder = mainOrderRepository.save(mainOrder);
        return mapMainOrderToDto(updatedMainOrder);
    }

    private String generateOrderReference() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private MainOrderResponseDto mapMainOrderToDto(MainOrder mainOrder) {
        MainOrderResponseDto dto = new MainOrderResponseDto();
        dto.setMainOrderId(mainOrder.getMainOrderId());
        dto.setOrderReference(mainOrder.getOrderReference());
        dto.setTotalAmount(mainOrder.getTotalAmount());
        dto.setStatus(mainOrder.getStatus());
        dto.setPaymentStatus(mainOrder.getPaymentStatus());
        dto.setPaymentMethod(mainOrder.getPaymentMethod());
        dto.setCreatedAt(mainOrder.getCreatedAt());
        dto.setUpdatedAt(mainOrder.getUpdatedAt());

        // Set customer information
        dto.setCustomerId((long) mainOrder.getCustomer().getId());
        dto.setCustomerName(mainOrder.getCustomer().getFirstName() + " " + mainOrder.getCustomer().getLastName());

        // Set address information if available
        if (mainOrder.getShippingAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setId(mainOrder.getShippingAddress().getId());
            addressDto.setFullName(mainOrder.getShippingAddress().getFullName());
            addressDto.setStreetAddress(mainOrder.getShippingAddress().getStreetAddress());
            addressDto.setCity(mainOrder.getShippingAddress().getCity());
            addressDto.setState(mainOrder.getShippingAddress().getState());
            addressDto.setZipCode(mainOrder.getShippingAddress().getZipCode());
            addressDto.setCountry(mainOrder.getShippingAddress().getCountry());
            dto.setShippingAddress(addressDto);
        }

        // Map sub-orders
        List<OrderResponseDto> subOrderDtos = new ArrayList<>();
        for (Order subOrder : mainOrder.getSubOrders()) {
            OrderResponseDto subOrderDto = mapSubOrderToDto(subOrder);
            subOrderDtos.add(subOrderDto);
        }
        dto.setSubOrders(subOrderDtos);

        return dto;
    }

    private OrderResponseDto mapSubOrderToDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Set store information
        dto.setStoreId(order.getStore().getStoreId());
        dto.setStoreName(order.getStore().getStoreName());

        // Set order items
        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setOrderItemId(item.getOrderItemId());
            itemDto.setProductId(item.getProduct().getProductId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPrice(item.getPrice());
            itemDto.setSubtotal(item.getPrice() * item.getQuantity());
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);

        return dto;
    }
}
