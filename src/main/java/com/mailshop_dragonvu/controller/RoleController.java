package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.request.RoleRequest;
import com.mailshop_dragonvu.dto.response.ApiResponse;
import com.mailshop_dragonvu.dto.response.RoleResponse;
import com.mailshop_dragonvu.service.RoleService;
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
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Role Management", description = "Role management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new role")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success("Role created successfully", roleService.createRole(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role by ID")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success("Role updated successfully", roleService.updateRole(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name")
    public ApiResponse<RoleResponse> getRoleByName(@PathVariable String name) {
        return ApiResponse.success(roleService.getRoleByName(name));
    }

    @GetMapping
    @Operation(summary = "Get all roles with pagination")
    public ApiResponse<Page<RoleResponse>> getAllRoles(Pageable pageable) {
        return ApiResponse.success(roleService.getAllRoles(pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role by ID")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success("Role deleted successfully");
    }

}
