package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.auth.RoleRequest;
import com.mailshop_dragonvu.dto.auth.RoleResponse;
import com.mailshop_dragonvu.entity.RoleEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-20T23:43:41+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleEntity toEntity(RoleRequest request) {
        if ( request == null ) {
            return null;
        }

        RoleEntity.RoleEntityBuilder roleEntity = RoleEntity.builder();

        roleEntity.description( request.getDescription() );
        roleEntity.name( request.getName() );

        return roleEntity.build();
    }

    @Override
    public RoleResponse toResponse(RoleEntity roleEntity) {
        if ( roleEntity == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.createdAt( roleEntity.getCreatedAt() );
        roleResponse.description( roleEntity.getDescription() );
        roleResponse.id( roleEntity.getId() );
        roleResponse.name( roleEntity.getName() );
        roleResponse.updatedAt( roleEntity.getUpdatedAt() );

        return roleResponse.build();
    }

    @Override
    public void updateEntity(RoleEntity roleEntity, RoleRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getDescription() != null ) {
            roleEntity.setDescription( request.getDescription() );
        }
        if ( request.getName() != null ) {
            roleEntity.setName( request.getName() );
        }
    }
}
