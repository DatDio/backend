package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.ranks.UserRankInfoDTO;
import com.mailshop_dragonvu.dto.users.*;
import com.mailshop_dragonvu.entity.RoleEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.entity.WalletEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.UserMapper;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.repository.WalletRepository;
import com.mailshop_dragonvu.service.RankService;
import com.mailshop_dragonvu.service.UserService;
import com.mailshop_dragonvu.utils.Constants;
import com.mailshop_dragonvu.utils.EnumParseUtils;
import com.mailshop_dragonvu.utils.Utils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RankService rankService;

    @Override
    @Transactional
    //@CacheEvict(value = "users", allEntries = true)
    public UserResponseDTO createUser(UserCreateDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserEntity userEntity = userMapper.toEntity(request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default USER role
        RoleEntity userRoleEntity = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        userEntity.getRoles().add(userRoleEntity);

        userEntity = userRepository.save(userEntity);
        log.info("User created successfully with ID: {}", userEntity.getId());

        return userMapper.toResponse(userEntity);
    }

    @Override
    @Transactional
    //@CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UserUpdateDTO request, Long currentUserId) {
        log.info("Updating user with ID: {}", id);

        // Get current user to check if admin
        UserEntity currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> Constants.ROLE_ADMIN.equals(role.getName()));

        // Check if user is admin OR is updating their own profile
        if (!isAdmin && !id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().equals(userEntity.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        userMapper.updateEntity(userEntity, request);

        // Handle status update (only admin can change status)
        if (isAdmin && request.getStatus() != null) {
            ActiveStatusEnum statusEnum = ActiveStatusEnum.fromKey(request.getStatus());
            if (statusEnum != null) {
                userEntity.setStatus(statusEnum);
            }
        }

        // Handle roles update (only admin can change roles)
        if (isAdmin && request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<RoleEntity> newRoles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)))
                    .collect(Collectors.toSet());
            userEntity.setRoles(newRoles);
        }

        // Handle collaborator update (only admin can change)
        if (isAdmin) {
            if (request.getIsCollaborator() != null) {
                userEntity.setIsCollaborator(request.getIsCollaborator());
            }
            if (request.getBonusPercent() != null) {
                userEntity.setBonusPercent(request.getBonusPercent());
            }
        }

        userEntity = userRepository.save(userEntity);

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponse(userEntity);
    }

    @Override
    //@Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Get wallet info
        WalletEntity wallet = walletRepository.findByUserId(id).orElse(null);
        Long balance = wallet != null ? wallet.getBalance() : 0L;
        Long totalDeposit = wallet != null ? wallet.getTotalDeposited() : 0L;
        Long totalSpent = wallet != null ? wallet.getTotalSpent() : 0L;

        UserResponseDTO response = userMapper.toResponse(userEntity);
        response.setBalance(balance);
        response.setTotalDeposit(totalDeposit);
        response.setTotalSpent(totalSpent);

        // Get user rank info
        try {
            var rankInfo = rankService.getUserRankInfo(id);
            response.setRank(rankInfo);
        } catch (Exception e) {
            log.warn("Could not get rank info for user {}: {}", id, e.getMessage());
        }

        return response;
    }

    @Override
    //@Cacheable(value = "users", key = "#id")
    public UserResponseClientDTO getUserByIdForClient(Long id) {
        log.debug("Fetching user by ID: {}", id);
        UserRankInfoDTO rankInfo = null;
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Get wallet info
        WalletEntity wallet = walletRepository.findByUserId(id).orElse(null);
        Long balance = wallet != null ? wallet.getBalance() : 0L;
        Long totalDeposit = wallet != null ? wallet.getTotalDeposited() : 0L;
        Long totalSpent = wallet != null ? wallet.getTotalSpent() : 0L;

        // Get user rank info
        try {
            rankInfo = rankService.getUserRankInfo(id);
        } catch (Exception e) {
            log.warn("Could not get rank info for user {}: {}", id, e.getMessage());
        }

        UserResponseClientDTO response = new UserResponseClientDTO().builder()
                .balance(balance)
                //.phone(userEntity.getPhone())
                //.address(userEntity.getAddress())
                .email(userEntity.getEmail())
                //.fullName(userEntity.getFullName())
                .totalDeposit(totalDeposit)
                .totalSpent(totalSpent)
                .rankName(rankInfo.getRankName())
                .bonusPercent(rankInfo.getBonusPercent())
                .isCollaborator(userEntity.getIsCollaborator())
                .ctvBonusPercent(userEntity.getBonusPercent())
                .build();

        return response;
    }

    @Override
    //@Cacheable(value = "users", key = "#email")
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toResponse(userEntity);
    }

    @Override
    public Page<UserResponseDTO> search(UserFilterDTO userFilterDTO) {
        Sort sort = Utils.generatedSort(userFilterDTO.getSort());
        Pageable pageable = PageRequest.of(userFilterDTO.getPage(), userFilterDTO.getLimit(), sort);

        Specification<UserEntity> specification = getSearchSpecification(userFilterDTO);
        Page<UserResponseDTO> page = userRepository.findAll(specification, pageable)
                .map(userMapper::toResponse);

        return page;
    }

    private Specification<UserEntity> getSearchSpecification(
            final UserFilterDTO request) {
        return new Specification<UserEntity>() {

            private static final long serialVersionUID = 6345534328548406667L;

            @Override
            @Nullable
            public Predicate toPredicate(@NonNull Root<UserEntity> root,
                                         @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (Strings.isNotBlank(request.getEmail())) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                            "%" + request.getEmail().trim().toLowerCase() + "%"));
                }

                if (Strings.isNotBlank(request.getFullName())) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")),
                            "%" + request.getFullName().trim().toLowerCase() + "%"));
                }

                if (!ObjectUtils.isEmpty(request.getStatus()) && !request.getStatus().isBlank()) {
                    Set<ActiveStatusEnum> statusSet = EnumParseUtils.parseEnumSetByKey(
                            request.getStatus(),
                            ActiveStatusEnum::fromKey
                    );
                    predicates.add(root.get("status").in(statusSet));
                }

                // Filter by collaborator type
                if (request.getIsCollaborator() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isCollaborator"), request.getIsCollaborator()));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

        };
    }

    @Override
    @Transactional
    //@CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userEntity.setStatus(ActiveStatusEnum.INACTIVE);
        userRepository.save(userEntity);

    }

    @Override
    @Transactional
    //@CacheEvict(value = "users", key = "#userId")
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<RoleEntity> roleEntities = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());

        userEntity.getRoles().addAll(roleEntities);
        userRepository.save(userEntity);

    }

    @Override
    @Transactional
    // @CacheEvict(value = "users", key = "#userId")
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        log.info("Removing roles from user ID: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<RoleEntity> rolesToRemove = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());

        userEntity.getRoles().removeAll(rolesToRemove);
        userRepository.save(userEntity);

    }

}
