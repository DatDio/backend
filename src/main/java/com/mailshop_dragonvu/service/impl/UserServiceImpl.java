package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.request.UserCreateRequest;
import com.mailshop_dragonvu.dto.request.UserUpdateRequest;
import com.mailshop_dragonvu.dto.response.UserResponse;
import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.UserMapper;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        user.getRoles().add(userRole);

        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        userMapper.updateEntity(user, request);
        user = userRepository.save(user);

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setStatus("DELETED");
        userRepository.save(user);

        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("Assigning roles to user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());

        user.getRoles().addAll(roles);
        userRepository.save(user);

        log.info("Roles assigned successfully to user ID: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        log.info("Removing roles from user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<Role> rolesToRemove = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());

        user.getRoles().removeAll(rolesToRemove);
        userRepository.save(user);

        log.info("Roles removed successfully from user ID: {}", userId);
    }

}
