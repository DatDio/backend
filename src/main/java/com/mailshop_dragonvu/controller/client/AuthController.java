package com.mailshop_dragonvu.controller.client;
import com.mailshop_dragonvu.dto.auth.LoginRequest;
import com.mailshop_dragonvu.dto.auth.RefreshTokenRequest;
import com.mailshop_dragonvu.dto.auth.RegisterRequest;
import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.auth.AuthResponse;
import com.mailshop_dragonvu.service.auth.AuthService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(Constants.API_PATH.AUTH)
@RequiredArgsConstructor
@Tag(name = "Xác thực", description = "Các API liên quan đến xác thực")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đăng ký người dùng mới")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("User registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập người dùng")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google token")
    public ApiResponse<AuthResponse> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        return ApiResponse.success("Google login successful", authService.googleLogin(idToken));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed successfully", authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất người dùng")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.success("Logout successful");
    }

}
