package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.request.LoginRequest;
import com.mailshop_dragonvu.dto.request.RefreshTokenRequest;
import com.mailshop_dragonvu.dto.request.RegisterRequest;
import com.mailshop_dragonvu.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

}
