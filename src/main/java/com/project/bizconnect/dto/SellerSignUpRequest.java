package com.project.bizconnect.dto;

import lombok.Data;

@Data
public class SellerSignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
