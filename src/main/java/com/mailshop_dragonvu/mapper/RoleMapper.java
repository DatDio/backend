package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import com.mailshop_dragonvu.entity.Role;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "users", ignore = true)
    void updateEntity(@MappingTarget Role role, RoleRequest request);

}
