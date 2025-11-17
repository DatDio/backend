package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.request.UserCreateRequest;
import com.mailshop_dragonvu.dto.request.UserUpdateRequest;
import com.mailshop_dragonvu.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    Page<UserResponse> getAllUsers(Pageable pageable);

    void deleteUser(Long id);

    void assignRolesToUser(Long userId, List<Long> roleIds);

    void removeRolesFromUser(Long userId, List<Long> roleIds);

}
