package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.request.RoleRequest;
import com.mailshop_dragonvu.dto.response.RoleResponse;
import com.mailshop_dragonvu.entity.Role;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    void updateEntity(@MappingTarget Role role, RoleRequest request);

}
