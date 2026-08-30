package com.memora.modules.user.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.user.dto.UserResponse;
import com.memora.modules.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing authenticated user profile operations.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = userService.getCurrentUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }
}
