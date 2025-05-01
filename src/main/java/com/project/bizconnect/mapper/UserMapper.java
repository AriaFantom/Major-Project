package com.project.bizconnect.mapper;

import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.entity.User;

public class UserMapper {

    public static UserDto mapUserToUserDto(User user) {
        return new UserDto(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(), user.getRole()
        );
    }
    public static User mapUserDtoToUser(UserDto userDto) {
        return new User(
                userDto.getId(), userDto.getFirstName(), userDto.getLastName(), userDto.getEmail(), userDto.getPassword(), userDto.getRole()
        );
    }
}
