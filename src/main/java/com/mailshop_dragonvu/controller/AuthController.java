package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.request.LoginRequest;
import com.mailshop_dragonvu.dto.request.RefreshTokenRequest;
import com.mailshop_dragonvu.dto.request.RegisterRequest;
import com.mailshop_dragonvu.dto.response.ApiResponse;
import com.mailshop_dragonvu.dto.response.AuthResponse;
import com.mailshop_dragonvu.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("User registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed successfully", authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.success("Logout successful");
    }

}
