package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.users.UserCreateDTO;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
import com.mailshop_dragonvu.dto.users.UserUpdateDTO;
import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "authProvider", expression = "java(com.mailshop_dragonvu.enums.AuthProvider.LOCAL)")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User toEntity(UserCreateDTO request);

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "authProvider", expression = "java(user.getAuthProvider().name())")
    UserResponseDTO toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateDTO request);

    default Set<String> mapRolesToStrings(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

}
