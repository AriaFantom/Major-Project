package com.project.bizconnect.dto;

import lombok.Data;

@Data
public class SignInRequest {

    private String email;
    private String password;
}
