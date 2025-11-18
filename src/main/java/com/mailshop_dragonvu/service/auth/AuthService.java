package com.mailshop_dragonvu.service.auth;

import com.mailshop_dragonvu.dto.auth.LoginRequest;
import com.mailshop_dragonvu.dto.auth.RefreshTokenRequest;
import com.mailshop_dragonvu.dto.auth.RegisterRequest;
import com.mailshop_dragonvu.dto.auth.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(String idToken);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

}
