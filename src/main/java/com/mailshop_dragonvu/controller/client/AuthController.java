package com.mailshop_dragonvu.controller.client;
import com.mailshop_dragonvu.dto.auth.*;
import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.service.auth.AuthService;
import com.mailshop_dragonvu.service.MessageService;
import com.mailshop_dragonvu.service.RecaptchaService;
import com.mailshop_dragonvu.utils.Constants;
import com.mailshop_dragonvu.utils.MessageKeys;
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
    private final RecaptchaService recaptchaService;
    private final MessageService messageService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đăng ký người dùng mới")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        recaptchaService.verify(request.getRecaptchaToken(), "register");
        return ApiResponse.success(messageService.getMessage(MessageKeys.Auth.REGISTERED), authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập người dùng")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        recaptchaService.verify(request.getRecaptchaToken(), "login");
        return ApiResponse.success(messageService.getMessage(MessageKeys.Auth.LOGIN), authService.login(request));
    }
    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success(messageService.getMessage(MessageKeys.Auth.PASSWORD_CHANGED));
    }
    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google token")
    public ApiResponse<AuthResponse> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        return ApiResponse.success(messageService.getMessage(MessageKeys.Auth.GOOGLE_LOGIN), authService.googleLogin(idToken));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(messageService.getMessage(MessageKeys.Auth.TOKEN_REFRESHED), authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất người dùng")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.success(messageService.getMessage(MessageKeys.Auth.LOGOUT));
    }

}


