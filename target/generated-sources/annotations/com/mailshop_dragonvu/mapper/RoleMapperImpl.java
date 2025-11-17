package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.request.RoleRequest;
import com.mailshop_dragonvu.dto.response.PermissionResponse;
import com.mailshop_dragonvu.dto.response.RoleResponse;
import com.mailshop_dragonvu.entity.Permission;
import com.mailshop_dragonvu.entity.Role;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-17T22:47:04+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Autowired
    private PermissionMapper permissionMapper;

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
        roleResponse.permissions( permissionSetToPermissionResponseSet( role.getPermissions() ) );
        roleResponse.status( role.getStatus() );
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

    protected Set<PermissionResponse> permissionSetToPermissionResponseSet(Set<Permission> set) {
        if ( set == null ) {
            return null;
        }

        Set<PermissionResponse> set1 = new LinkedHashSet<PermissionResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Permission permission : set ) {
            set1.add( permissionMapper.toResponse( permission ) );
        }

        return set1;
    }
}
