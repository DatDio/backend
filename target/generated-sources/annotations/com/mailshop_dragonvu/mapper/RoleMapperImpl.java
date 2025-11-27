package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import com.mailshop_dragonvu.entity.RoleEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T17:00:06+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleEntity toEntity(RoleRequest request) {
        if ( request == null ) {
            return null;
        }

        RoleEntity.RoleEntityBuilder roleEntity = RoleEntity.builder();

        roleEntity.name( request.getName() );
        roleEntity.description( request.getDescription() );

        return roleEntity.build();
    }

    @Override
    public RoleResponse toResponse(RoleEntity roleEntity) {
        if ( roleEntity == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.id( roleEntity.getId() );
        roleResponse.name( roleEntity.getName() );
        roleResponse.description( roleEntity.getDescription() );
        roleResponse.createdAt( roleEntity.getCreatedAt() );
        roleResponse.updatedAt( roleEntity.getUpdatedAt() );

        return roleResponse.build();
    }

    @Override
    public void updateEntity(RoleEntity roleEntity, RoleRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            roleEntity.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            roleEntity.setDescription( request.getDescription() );
        }
    }
}
