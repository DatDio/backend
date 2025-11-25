package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.apikeys.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKeyEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-25T21:28:23+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class ApiKeyMapperImpl implements ApiKeyMapper {

    @Override
    public ApiKeyResponse toResponse(ApiKeyEntity apiKeyEntity) {
        if ( apiKeyEntity == null ) {
            return null;
        }

        ApiKeyResponse.ApiKeyResponseBuilder apiKeyResponse = ApiKeyResponse.builder();

        apiKeyResponse.id( apiKeyEntity.getId() );
        apiKeyResponse.name( apiKeyEntity.getName() );
        apiKeyResponse.status( apiKeyEntity.getStatus() );
        apiKeyResponse.createdAt( apiKeyEntity.getCreatedAt() );
        apiKeyResponse.lastUsedAt( apiKeyEntity.getLastUsedAt() );

        return apiKeyResponse.build();
    }
}
