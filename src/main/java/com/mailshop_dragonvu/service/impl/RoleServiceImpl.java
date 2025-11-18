package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.RoleMapper;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.ROLE_ALREADY_EXISTS);
        }

        Role role = roleMapper.toEntity(request);
        role = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", role.getId());

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public RoleResponse updateRole(Long id, RoleRequest request) {
        log.info("Updating role with ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        if (request.getName() != null && !request.getName().equals(role.getName()) && roleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.ROLE_ALREADY_EXISTS);
        }

        roleMapper.updateEntity(role, request);
        role = roleRepository.save(role);
        log.info("Role updated successfully with ID: {}", id);

        return roleMapper.toResponse(role);
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    public RoleResponse getRoleById(Long id) {
        log.debug("Fetching role by ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        return roleMapper.toResponse(role);
    }

    @Override
    @Cacheable(value = "roles", key = "#name")
    public RoleResponse getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);

        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        return roleMapper.toResponse(role);
    }

    @Override
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        log.debug("Fetching all roles with pagination");
        return roleRepository.findAll(pageable)
                .map(roleMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        roleRepository.save(role);
    }

}
