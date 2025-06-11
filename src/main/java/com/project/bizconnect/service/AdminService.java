package com.project.bizconnect.service;

import com.project.bizconnect.dto.*;
import com.project.bizconnect.entity.Role;

import java.util.List;

public interface AdminService {
    List<UserDto> getUsersByRole(Role role);
    List<UserDto> getAllUsers();
    UserDto changeUserRole(int userId, RoleChangeRequest request);
    List<StoreWithUserDto> getAllStoresWithUsers();
    StoreWithUserDto toggleStoreVerification(Long storeId, boolean verificationStatus);

    // New methods for customer and seller statistics
    List<CustomerStatsDto> getAllCustomersWithStats();
    List<SellerStatsDto> getAllSellersWithStores();

    // New methods for order management
    List<MainOrderResponseDto> getAllOrdersWithDetails();
    MainOrderResponseDto getOrderByIdWithDetails(Long mainOrderId);

    // Methods for updating order statuses
    MainOrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto statusUpdate);
    SubOrderDto updateSubOrderStatus(Long subOrderId, SubOrderStatusUpdateDto statusUpdate);

    // New admin dashboard statistics methods
    AdminDashboardStatsDto getDashboardStatistics();
    List<CategorySalesPercentageDto> getCategorySalesPercentages();

    // Monthly sales chart data
    List<MonthlySalesDto> getMonthlySalesData(Integer year);

    // Top performing stores data
    List<ShopPerformanceDto> getTopPerformingStores(Integer limit);
}
