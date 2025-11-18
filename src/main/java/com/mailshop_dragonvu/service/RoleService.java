package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    RoleResponse getRoleById(Long id);

    RoleResponse getRoleByName(String name);

    Page<RoleResponse> getAllRoles(Pageable pageable);

    void deleteRole(Long id);

}
