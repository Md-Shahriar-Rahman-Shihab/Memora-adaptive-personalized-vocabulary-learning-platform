package com.memora.modules.user.service;

import com.memora.modules.user.dto.AuthResponse;
import com.memora.modules.user.dto.LoginRequest;
import com.memora.modules.user.dto.RegistrationRequest;

/**
 * Service interface for user registration and authentication workflows.
 * Demonstrates the Interface Segregation and Dependency Inversion principles.
 */
public interface AuthService {

    AuthResponse register(RegistrationRequest request);

    AuthResponse login(LoginRequest request);
}
