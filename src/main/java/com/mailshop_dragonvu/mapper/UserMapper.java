package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.users.UserCreateDTO;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
import com.mailshop_dragonvu.dto.users.UserUpdateDTO;
import com.mailshop_dragonvu.entity.RoleEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "authProvider", expression = "java(com.mailshop_dragonvu.enums.AuthProvider.LOCAL)")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    UserEntity toEntity(UserCreateDTO request);

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(userEntity.getRoles()))")
    @Mapping(target = "authProvider", expression = "java(userEntity.getAuthProvider().name())")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "isCollaborator", source = "isCollaborator")
    @Mapping(target = "bonusPercent", source = "bonusPercent")
    UserResponseDTO toResponse(UserEntity userEntity);

    default Integer mapStatus(ActiveStatusEnum status) {
        return status != null ? status.getKey() : null;
    }

    default ActiveStatusEnum mapStatusToEnum(Integer status) {
        return status != null ? ActiveStatusEnum.fromKey(status) : null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    void updateEntity(@MappingTarget UserEntity userEntity, UserUpdateDTO request);

    default Set<String> mapRolesToStrings(Set<RoleEntity> roleEntities) {
        return roleEntities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }
}
