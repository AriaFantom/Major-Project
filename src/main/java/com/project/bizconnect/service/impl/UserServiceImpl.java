package com.project.bizconnect.service.impl;

import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.mapper.UserMapper;
import com.project.bizconnect.repository.UsersRepository;
import com.project.bizconnect.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private UsersRepository usersRepository;
    @Override
    public UserDto createUser(UserDto userDto) {

        User user = UserMapper.mapUserDtoToUser(userDto);
        User savedUser = usersRepository.save(user);

        return UserMapper.mapUserToUserDto(savedUser);
    }
}
