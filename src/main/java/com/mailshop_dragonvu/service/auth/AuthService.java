package com.mailshop_dragonvu.service.auth;

import com.mailshop_dragonvu.dto.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(ChangePasswordRequest request);

    AuthResponse googleLogin(String idToken);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

}
