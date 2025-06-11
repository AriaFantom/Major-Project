package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.*;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.repository.UsersRepository;
import com.project.bizconnect.repository.OrderRepository;
import com.project.bizconnect.repository.MainOrderRepository;
import com.project.bizconnect.repository.ProductRepository;
import com.project.bizconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsersRepository usersRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final MainOrderRepository mainOrderRepository;
    private final ProductRepository productRepository;

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    @Override
    public List<UserDto> getUsersByRole(Role role) {
        return usersRepository.findAllByRole(role).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllUsers() {
        return usersRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto changeUserRole(int userId, RoleChangeRequest request) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(request.getRole());
        User updated = usersRepository.save(user);
        return mapToDto(updated);
    }

    @Override
    public List<StoreWithUserDto> getAllStoresWithUsers() {
        List<Store> allStores = storeRepository.findAll();

        return allStores.stream()
            .map(store -> {
                User owner = store.getOwner();
                String imageUrl = null;
                if (store.getImageUrl() != null && !store.getImageUrl().isEmpty()) {
                    imageUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/shop/stores/image/")
                            .path(store.getImageUrl())
                            .toUriString();
                }

                return StoreWithUserDto.builder()
                    .storeId(store.getStoreId())
                    .storeName(store.getStoreName())
                    .description(store.getDescription())
                    .verified(store.isVerified())
                    .email(store.getEmail())
                    .phoneNumber(store.getPhoneNumber())
                    .address(store.getAddress())
                    .websiteUrl(store.getWebsiteUrl())
                    .imageUrl(imageUrl)
                    .createdAt(store.getCreatedAt())
                    .updatedAt(store.getUpdatedAt())
                    .userId(owner.getId())
                    .userFirstName(owner.getFirstName())
                    .userLastName(owner.getLastName())
                    .userName(owner.getFirstName() + " " + owner.getLastName())
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    public StoreWithUserDto toggleStoreVerification(Long storeId, boolean verificationStatus) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));

        store.setVerified(verificationStatus);
        Store updatedStore = storeRepository.save(store);

        User owner = updatedStore.getOwner();
        String imageUrl = null;
        if (updatedStore.getImageUrl() != null && !updatedStore.getImageUrl().isEmpty()) {
            imageUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/shop/stores/image/")
                    .path(updatedStore.getImageUrl())
                    .toUriString();
        }

        return StoreWithUserDto.builder()
                .storeId(updatedStore.getStoreId())
                .storeName(updatedStore.getStoreName())
                .description(updatedStore.getDescription())
                .verified(updatedStore.isVerified())
                .email(updatedStore.getEmail())
                .phoneNumber(updatedStore.getPhoneNumber())
                .address(updatedStore.getAddress())
                .websiteUrl(updatedStore.getWebsiteUrl())
                .imageUrl(imageUrl)
                .createdAt(updatedStore.getCreatedAt())
                .updatedAt(updatedStore.getUpdatedAt())
                .userId(owner.getId())
                .userFirstName(owner.getFirstName())
                .userLastName(owner.getLastName())
                .userName(owner.getFirstName() + " " + owner.getLastName()) // Keep for backward compatibility
                .build();
    }

    @Override
    public List<CustomerStatsDto> getAllCustomersWithStats() {
        // Get all users with CUSTOMER role
        List<User> customers = usersRepository.findAllByRole(Role.CUSTOMER);

        // Get all orders
        List<Order> allOrders = orderRepository.findAll();

        // Create a map of customer IDs to their orders
        Map<Integer, List<Order>> customerOrders = new HashMap<>();
        for (Order order : allOrders) {
            User customer = order.getCustomer();
            if (customer != null) {
                Integer customerId = customer.getId();
                customerOrders.computeIfAbsent(customerId, k -> new ArrayList<>()).add(order);
            }
        }

        // Map each customer to CustomerStatsDto with order statistics
        return customers.stream()
            .map(customer -> {
                CustomerStatsDto dto = new CustomerStatsDto();
                dto.setUserId(customer.getId());
                dto.setFirstName(customer.getFirstName());
                dto.setLastName(customer.getLastName());
                dto.setEmail(customer.getEmail());
                dto.setJoiningDate(customer.getCreatedAt());

                // Get orders for this customer
                List<Order> orders = customerOrders.getOrDefault(customer.getId(), Collections.emptyList());

                // Calculate total orders
                dto.setTotalOrders(orders.size());

                // Calculate total spend
                double totalSpent = orders.stream()
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
                dto.setTotalSpent(totalSpent);

                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SellerStatsDto> getAllSellersWithStores() {
        // Get all users with SELLER role
        List<User> sellers = usersRepository.findAllByRole(Role.SELLER);

        // Create a map of seller IDs to their stores
        Map<Integer, List<Store>> sellerStores = new HashMap<>();
        List<Store> allStores = storeRepository.findAll();

        for (Store store : allStores) {
            User owner = store.getOwner();
            if (owner != null) {
                Integer sellerId = owner.getId();
                sellerStores.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(store);
            }
        }

        // Map each seller to SellerStatsDto with store information
        return sellers.stream()
            .map(seller -> {
                SellerStatsDto dto = new SellerStatsDto();
                dto.setUserId(seller.getId());
                dto.setFirstName(seller.getFirstName());
                dto.setLastName(seller.getLastName());
                dto.setEmail(seller.getEmail());
                dto.setJoiningDate(seller.getCreatedAt());

                // Get stores owned by this seller
                List<Store> stores = sellerStores.getOrDefault(seller.getId(), Collections.emptyList());

                // Map stores to SellerStoreDto
                List<SellerStatsDto.SellerStoreDto> storeDtos = stores.stream()
                    .map(store -> {
                        SellerStatsDto.SellerStoreDto storeDto = new SellerStatsDto.SellerStoreDto();
                        storeDto.setStoreId(store.getStoreId());
                        storeDto.setStoreName(store.getStoreName());

                        // Generate full URL for store image if available
                        if (store.getImageUrl() != null && !store.getImageUrl().isEmpty()) {
                            storeDto.setImageUrl(org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/shop/stores/image/")
                                .path(store.getImageUrl())
                                .toUriString());
                        } else {
                            storeDto.setImageUrl(null);
                        }

                        return storeDto;
                    })
                    .collect(Collectors.toList());

                dto.setStores(storeDtos);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<MainOrderResponseDto> getAllOrdersWithDetails() {
        List<MainOrder> allMainOrders = mainOrderRepository.findAll();
        return allMainOrders.stream()
                .map(this::mapToMainOrderResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public MainOrderResponseDto getOrderByIdWithDetails(Long mainOrderId) {
        MainOrder mainOrder = mainOrderRepository.findById(mainOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Main order not found with ID: " + mainOrderId));

        return mapToMainOrderResponseDto(mainOrder);
    }

    @Override
    public MainOrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto statusUpdate) {
        MainOrder mainOrder = mainOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Main order not found with ID: " + orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusUpdate.getStatus().toUpperCase());
            mainOrder.setStatus(newStatus);

            // If setting order to DELIVERED, update all sub-orders too
            if (newStatus == OrderStatus.DELIVERED) {
                for (Order subOrder : mainOrder.getSubOrders()) {
                    subOrder.setStatus(OrderStatus.DELIVERED);
                    orderRepository.save(subOrder);
                }
            }

            MainOrder savedOrder = mainOrderRepository.save(mainOrder);
            return mapToMainOrderResponseDto(savedOrder);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + statusUpdate.getStatus());
        }
    }

    @Override
    public SubOrderDto updateSubOrderStatus(Long subOrderId, SubOrderStatusUpdateDto statusUpdate) {
        Order subOrder = orderRepository.findById(subOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-order not found with ID: " + subOrderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusUpdate.getStatus().toUpperCase());
            subOrder.setStatus(newStatus);

            Order savedOrder = orderRepository.save(subOrder);

            // Update main order status if this sub-order is associated with a main order
            if (savedOrder.getMainOrder() != null) {
                MainOrder mainOrder = savedOrder.getMainOrder();
                List<Order> allSubOrders = mainOrder.getSubOrders();

                // Determine appropriate main order status based on sub-orders
                OrderStatus mainOrderStatus = determineMainOrderStatus(allSubOrders);

                // Update main order if status needs to change
                if (mainOrder.getStatus() != mainOrderStatus) {
                    mainOrder.setStatus(mainOrderStatus);
                    mainOrderRepository.save(mainOrder);
                }
            }

            return mapToSubOrderDto(savedOrder);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + statusUpdate.getStatus());
        }
    }

    @Override
    public AdminDashboardStatsDto getDashboardStatistics() {
        // Count total products in the system
        long totalProducts = productRepository.count();

        // Count total stores in the system
        long totalStores = storeRepository.count();

        // Count total customers (users with CUSTOMER role)
        long totalCustomers = usersRepository.findAllByRole(Role.CUSTOMER).size();

        // Calculate total sales across all orders
        List<Order> allOrders = orderRepository.findAll();
        double totalSales = allOrders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Build and return dashboard statistics
        return AdminDashboardStatsDto.builder()
                .totalSales((long) totalSales)
                .totalProducts(totalProducts)
                .totalStores(totalStores)
                .totalCustomers(totalCustomers)
                .build();
    }

    @Override
    public List<CategorySalesPercentageDto> getCategorySalesPercentages() {
        // Get all completed orders
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .collect(Collectors.toList());

        // Calculate total sales amount across all categories
        double totalSalesAmount = completedOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Map to store category ID and its sales amount
        Map<Long, Double> categorySales = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        // Process each order and its items
        for (Order order : completedOrders) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null && product.getCategory() != null) {
                    Category category = product.getCategory();
                    Long categoryId = category.getId();
                    String categoryName = category.getName();

                    // Store category name for reference
                    categoryNames.putIfAbsent(categoryId, categoryName);

                    // Calculate sales amount for this item
                    double salesAmount = item.getPrice() * item.getQuantity();

                    // Add to category sales
                    categorySales.put(categoryId,
                            categorySales.getOrDefault(categoryId, 0.0) + salesAmount);
                }
            }
        }

        // Create DTOs with sales percentages
        List<CategorySalesPercentageDto> result = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : categorySales.entrySet()) {
            Long categoryId = entry.getKey();
            Double salesAmount = entry.getValue();

            // Calculate percentage
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalSalesAmount > 0) {
                percentage = BigDecimal.valueOf(salesAmount / totalSalesAmount * 100)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // Create DTO
            result.add(CategorySalesPercentageDto.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryNames.get(categoryId))
                    .salesPercentage(percentage)
                    .totalSalesAmount(BigDecimal.valueOf(salesAmount).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        // Sort by percentage descending
        result.sort((a, b) -> b.getSalesPercentage().compareTo(a.getSalesPercentage()));

        return result;
    }

    @Override
    public List<MonthlySalesDto> getMonthlySalesData(Integer year) {
        // If no year is provided, use the current year
        int targetYear = (year != null) ? year : java.time.LocalDate.now().getYear();

        // Get all completed/paid orders
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .collect(Collectors.toList());

        // Initialize a map to hold monthly sales
        Map<Integer, BigDecimal> monthlySales = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlySales.put(i, BigDecimal.ZERO);
        }

        // Calculate sales for each month
        for (Order order : completedOrders) {
            // Extract the month and year from the order creation date
            LocalDateTime orderDate = order.getCreatedAt();
            int orderYear = orderDate.getYear();
            int orderMonth = orderDate.getMonthValue(); // 1-12

            // Only count orders from the requested year
            if (orderYear == targetYear) {
                // Add the order amount to the appropriate month
                BigDecimal currentAmount = monthlySales.get(orderMonth);
                BigDecimal orderAmount = BigDecimal.valueOf(order.getTotalAmount());
                monthlySales.put(orderMonth, currentAmount.add(orderAmount));
            }
        }

        // Create a list of MonthlySalesDto with month names
        List<MonthlySalesDto> result = new ArrayList<>();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (int i = 1; i <= 12; i++) {
            result.add(MonthlySalesDto.builder()
                    .name(monthNames[i-1])
                    .value(monthlySales.get(i))
                    .build());
        }

        return result;
    }

    @Override
    public List<ShopPerformanceDto> getTopPerformingStores(Integer limit) {
        // Default limit to 10 if not specified
        int maxResults = (limit != null && limit > 0) ? limit : 10;

        // Get all stores
        List<Store> allStores = storeRepository.findAll();

        // Get all completed/paid orders
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .collect(Collectors.toList());

        // Map to store store data and calculate metrics
        Map<Long, String> storeNames = new HashMap<>();
        Map<Long, BigDecimal> storeSales = new HashMap<>();
        Map<Long, Set<Integer>> storeCustomers = new HashMap<>();

        // Process each completed order
        for (Order order : completedOrders) {
            Store store = order.getStore();
            if (store != null) {
                Long storeId = store.getStoreId();
                Integer customerId = order.getCustomer().getId();
                BigDecimal orderAmount = BigDecimal.valueOf(order.getTotalAmount());

                // Store the store name for reference
                storeNames.putIfAbsent(storeId, store.getStoreName());

                // Add the customer to this store's set of unique customers
                storeCustomers.computeIfAbsent(storeId, k -> new HashSet<>()).add(customerId);

                // Add the sales amount for this store
                storeSales.put(storeId,
                        storeSales.getOrDefault(storeId, BigDecimal.ZERO).add(orderAmount));
            }
        }

        // Create performance DTOs for each store
        List<ShopPerformanceDto> performanceData = new ArrayList<>();
        for (Long storeId : storeNames.keySet()) {
            ShopPerformanceDto dto = ShopPerformanceDto.builder()
                    .name(storeNames.get(storeId))
                    .sales(storeSales.getOrDefault(storeId, BigDecimal.ZERO))
                    .customers((long) storeCustomers.getOrDefault(storeId, Collections.emptySet()).size())
                    .build();
            performanceData.add(dto);
        }

        // Sort by sales in descending order and limit the results
        return performanceData.stream()
                .sorted(Comparator.comparing(ShopPerformanceDto::getSales).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private OrderStatus determineMainOrderStatus(List<Order> subOrders) {
        // If all orders are DELIVERED, the main order is DELIVERED
        boolean allDelivered = subOrders.stream()
                .allMatch(order -> order.getStatus() == OrderStatus.DELIVERED);
        if (allDelivered) {
            return OrderStatus.DELIVERED;
        }

        // If at least one order is SHIPPED and none are in PROCESSING or PENDING, then main order is SHIPPED
        boolean anyShipped = subOrders.stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.SHIPPED);
        boolean noneProcessingOrPending = subOrders.stream()
                .noneMatch(order -> order.getStatus() == OrderStatus.PROCESSING ||
                                   order.getStatus() == OrderStatus.PENDING);
        if (anyShipped && noneProcessingOrPending) {
            return OrderStatus.SHIPPED;
        }

        // If any order is in PROCESSING, the main order is in PROCESSING
        boolean anyProcessing = subOrders.stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.PROCESSING);
        if (anyProcessing) {
            return OrderStatus.PROCESSING;
        }

        // Otherwise, keep the main order in PENDING
        return OrderStatus.PENDING;
    }

    private SubOrderDto mapToSubOrderDto(Order order) {
        SubOrderDto dto = new SubOrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Set store information
        Store store = order.getStore();
        if (store != null) {
            dto.setStoreId(store.getStoreId());
            dto.setStoreName(store.getStoreName());
        }

        // Map order items
        List<OrderItemResponseDto> orderItems = order.getItems().stream()
                .map(this::mapToOrderItemResponseDto)
                .collect(Collectors.toList());

        dto.setItems(orderItems);

        return dto;
    }

    private MainOrderResponseDto mapToMainOrderResponseDto(MainOrder mainOrder) {
        MainOrderResponseDto dto = new MainOrderResponseDto();
        dto.setMainOrderId(mainOrder.getMainOrderId());
        dto.setOrderReference(mainOrder.getOrderReference());
        dto.setTotalAmount(mainOrder.getTotalAmount());
        dto.setStatus(mainOrder.getStatus());
        dto.setPaymentStatus(mainOrder.getPaymentStatus());
        dto.setPaymentMethod(mainOrder.getPaymentMethod());
        dto.setCreatedAt(mainOrder.getCreatedAt());
        dto.setUpdatedAt(mainOrder.getUpdatedAt());

        // Map customer information
        User customer = mainOrder.getCustomer();
        if (customer != null) {
            dto.setCustomerId((long) customer.getId()); // Converting int to Long
            dto.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        }

        // Map address if available
        if (mainOrder.getShippingAddress() != null) {
            AddressDto addressDto = new AddressDto();
            Address address = mainOrder.getShippingAddress();
            addressDto.setId(address.getId());
            addressDto.setFullName(address.getFullName());
            addressDto.setStreetAddress(address.getStreetAddress());
            addressDto.setCity(address.getCity());
            addressDto.setState(address.getState());
            addressDto.setZipCode(address.getZipCode());
            addressDto.setCountry(address.getCountry());
            addressDto.setDefaultAddress(address.getDefaultAddress());
            dto.setShippingAddress(addressDto);
        }

        // Map suborders
        List<OrderResponseDto> subOrders = mainOrder.getSubOrders().stream()
                .map(this::mapToOrderResponseDto)
                .collect(Collectors.toList());

        dto.setSubOrders(subOrders);

        return dto;
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());

        // Map store information
        Store store = order.getStore();
        if (store != null) {
            dto.setStoreId(store.getStoreId());
            dto.setStoreName(store.getStoreName());
        }

        // Map order items
        List<OrderItemResponseDto> orderItems = order.getItems().stream()
                .map(this::mapToOrderItemResponseDto)
                .collect(Collectors.toList());

        dto.setItems(orderItems);

        return dto;
    }

    private OrderItemResponseDto mapToOrderItemResponseDto(OrderItem item) {
        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getPrice() * item.getQuantity());

        // Map product information
        Product product = item.getProduct();
        if (product != null) {
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getName());
            // If no getImage() method, use null or an empty string
            // dto.setProductImage(product.getImage());
            dto.setProductImage(null);
        }

        return dto;
    }
}
