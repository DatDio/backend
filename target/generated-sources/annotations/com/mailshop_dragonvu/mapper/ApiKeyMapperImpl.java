package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.apikeys.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKeyEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-20T23:43:41+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ApiKeyMapperImpl implements ApiKeyMapper {

    @Override
    public ApiKeyResponse toResponse(ApiKeyEntity apiKeyEntity) {
        if ( apiKeyEntity == null ) {
            return null;
        }

        ApiKeyResponse.ApiKeyResponseBuilder apiKeyResponse = ApiKeyResponse.builder();

        apiKeyResponse.createdAt( apiKeyEntity.getCreatedAt() );
        apiKeyResponse.id( apiKeyEntity.getId() );
        apiKeyResponse.lastUsedAt( apiKeyEntity.getLastUsedAt() );
        apiKeyResponse.name( apiKeyEntity.getName() );
        apiKeyResponse.status( apiKeyEntity.getStatus() );

        return apiKeyResponse.build();
    }
}
