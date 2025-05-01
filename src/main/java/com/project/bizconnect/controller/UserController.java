package com.project.bizconnect.controller;

import com.project.bizconnect.dto.UserDto;
import com.project.bizconnect.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {

    private UserService userservice;

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
     UserDto savedUser =   userservice.createUser(userDto);
     return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}
