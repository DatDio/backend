package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.request.PermissionRequest;
import com.mailshop_dragonvu.dto.response.PermissionResponse;
import com.mailshop_dragonvu.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-17T22:47:04+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission toEntity(PermissionRequest request) {
        if ( request == null ) {
            return null;
        }

        Permission.PermissionBuilder permission = Permission.builder();

        permission.name( request.getName() );
        permission.description( request.getDescription() );
        permission.resource( request.getResource() );
        permission.action( request.getAction() );

        return permission.build();
    }

    @Override
    public PermissionResponse toResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.id( permission.getId() );
        permissionResponse.name( permission.getName() );
        permissionResponse.description( permission.getDescription() );
        permissionResponse.resource( permission.getResource() );
        permissionResponse.action( permission.getAction() );
        permissionResponse.status( permission.getStatus() );
        permissionResponse.createdAt( permission.getCreatedAt() );
        permissionResponse.updatedAt( permission.getUpdatedAt() );

        return permissionResponse.build();
    }

    @Override
    public void updateEntity(Permission permission, PermissionRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            permission.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            permission.setDescription( request.getDescription() );
        }
        if ( request.getResource() != null ) {
            permission.setResource( request.getResource() );
        }
        if ( request.getAction() != null ) {
            permission.setAction( request.getAction() );
        }
    }
}
