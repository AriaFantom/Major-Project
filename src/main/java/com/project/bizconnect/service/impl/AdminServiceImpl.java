package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.dto.RoleChangeRequest;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.entity.Role;
import com.project.bizconnect.repository.UsersRepository;
import com.project.bizconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsersRepository usersRepository;

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
}
