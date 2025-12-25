package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
import com.mailshop_dragonvu.dto.users.UserUpdateDTO;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.UserService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_PATH.USERS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý ví", description = "Quản lý số dư người dùng")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserResponseDTO user = userService.getUserById(userPrincipal.getId());
        return ApiResponse.success(user);
    }
    @PutMapping("update/{id}")
    public ApiResponse<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success("Cập nhật thành công", userService.updateUser(id, request, userPrincipal.getId()));
    }
}
