package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.Order;
import com.project.bizconnect.repository.StoreRepository;
import com.project.bizconnect.repository.UsersRepository;
import com.project.bizconnect.repository.OrderRepository;
import com.project.bizconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsersRepository usersRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

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

                return StoreWithUserDto.builder()
                    // Store details
                    .storeId(store.getStoreId())
                    .storeName(store.getStoreName())
                    .description(store.getDescription())
                    .verified(store.isVerified())
                    .email(store.getEmail())
                    .phoneNumber(store.getPhoneNumber())
                    .address(store.getAddress())
                    .websiteUrl(store.getWebsiteUrl())
                    .createdAt(store.getCreatedAt())
                    .updatedAt(store.getUpdatedAt())
                    // User details
                    .userId(owner.getId())
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

        return StoreWithUserDto.builder()
                // Store details
                .storeId(updatedStore.getStoreId())
                .storeName(updatedStore.getStoreName())
                .description(updatedStore.getDescription())
                .verified(updatedStore.isVerified())
                .email(updatedStore.getEmail())
                .phoneNumber(updatedStore.getPhoneNumber())
                .address(updatedStore.getAddress())
                .websiteUrl(updatedStore.getWebsiteUrl())
                .createdAt(updatedStore.getCreatedAt())
                .updatedAt(updatedStore.getUpdatedAt())
                // User details
                .userId(owner.getId())
                .userName(owner.getFirstName() + " " + owner.getLastName())
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
                        return storeDto;
                    })
                    .collect(Collectors.toList());

                dto.setStores(storeDtos);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
