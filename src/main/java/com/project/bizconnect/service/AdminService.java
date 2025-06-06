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
}
