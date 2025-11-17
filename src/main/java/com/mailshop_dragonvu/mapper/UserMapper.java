package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.request.UserCreateRequest;
import com.mailshop_dragonvu.dto.request.UserUpdateRequest;
import com.mailshop_dragonvu.dto.response.UserResponse;
import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "authProvider", expression = "java(com.mailshop_dragonvu.enums.AuthProvider.LOCAL)")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "authProvider", expression = "java(user.getAuthProvider().name())")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);

    default Set<String> mapRolesToStrings(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

}
