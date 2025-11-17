package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.request.UserCreateRequest;
import com.mailshop_dragonvu.dto.request.UserUpdateRequest;
import com.mailshop_dragonvu.dto.response.ApiResponse;
import com.mailshop_dragonvu.dto.response.UserResponse;
import com.mailshop_dragonvu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("User created successfully", userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update user by ID")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("User updated successfully", userService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ApiResponse<UserResponse> getUserByEmail(@PathVariable String email) {
        return ApiResponse.success(userService.getUserByEmail(email));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with pagination")
    public ApiResponse<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ApiResponse.success(userService.getAllUsers(pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user by ID")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User deleted successfully");
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign roles to user")
    public ApiResponse<Void> assignRolesToUser(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        userService.assignRolesToUser(userId, roleIds);
        return ApiResponse.success("Roles assigned successfully");
    }

    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove roles from user")
    public ApiResponse<Void> removeRolesFromUser(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        userService.removeRolesFromUser(userId, roleIds);
        return ApiResponse.success("Roles removed successfully");
    }

}
