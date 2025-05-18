package com.project.bizconnect.service;

import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.dto.RoleChangeRequest;
import com.project.bizconnect.entity.Role;

import java.util.List;

public interface AdminService {
    List<UserDto> getUsersByRole(Role role);
    List<UserDto> getAllUsers();
    UserDto changeUserRole(int userId, RoleChangeRequest request);
}
