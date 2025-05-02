package com.project.bizconnect.service;

import com.project.bizconnect.dto.JwtAuthenticationResponse;
import com.project.bizconnect.dto.SignInRequest;
import com.project.bizconnect.dto.SignUpRequest;
import com.project.bizconnect.entity.User;

public interface AuthenticationService {

      User signUp(SignUpRequest signUpRequest);

      JwtAuthenticationResponse signin(SignInRequest signInRequest);
}
