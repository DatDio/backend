package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.request.RoleRequest;
import com.mailshop_dragonvu.dto.response.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    RoleResponse getRoleById(Long id);

    RoleResponse getRoleByName(String name);

    Page<RoleResponse> getAllRoles(Pageable pageable);

    void deleteRole(Long id);

}
