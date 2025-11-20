package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import com.mailshop_dragonvu.entity.RoleEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "users", ignore = true)
    RoleEntity toEntity(RoleRequest request);

    RoleResponse toResponse(RoleEntity roleEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "users", ignore = true)
    void updateEntity(@MappingTarget RoleEntity roleEntity, RoleRequest request);

}
