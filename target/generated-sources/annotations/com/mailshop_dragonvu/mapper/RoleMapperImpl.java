package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import com.mailshop_dragonvu.entity.Role;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T18:10:42+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public Role toEntity(RoleRequest request) {
        if ( request == null ) {
            return null;
        }

        Role.RoleBuilder role = Role.builder();

        role.name( request.getName() );
        role.description( request.getDescription() );

        return role.build();
    }

    @Override
    public RoleResponse toResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.id( role.getId() );
        roleResponse.name( role.getName() );
        roleResponse.description( role.getDescription() );
        roleResponse.createdAt( role.getCreatedAt() );
        roleResponse.updatedAt( role.getUpdatedAt() );

        return roleResponse.build();
    }

    @Override
    public void updateEntity(Role role, RoleRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            role.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            role.setDescription( request.getDescription() );
        }
    }
}
