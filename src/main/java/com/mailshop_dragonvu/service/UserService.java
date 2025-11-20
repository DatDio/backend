package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.users.UserCreateDTO;
import com.mailshop_dragonvu.dto.users.UserFilterDTO;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
import com.mailshop_dragonvu.dto.users.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserCreateDTO request);

    UserResponseDTO updateUser(Long id, UserUpdateDTO request);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getUserByEmail(String email);

    Page<UserResponseDTO> search(UserFilterDTO userFilterDTO);

    void deleteUser(Long id);

    void assignRolesToUser(Long userId, List<Long> roleIds);

    void removeRolesFromUser(Long userId, List<Long> roleIds);

}
