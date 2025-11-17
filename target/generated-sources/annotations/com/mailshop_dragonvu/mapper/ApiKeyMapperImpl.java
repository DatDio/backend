package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.response.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKey;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-17T22:47:05+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class ApiKeyMapperImpl implements ApiKeyMapper {

    @Override
    public ApiKeyResponse toResponse(ApiKey apiKey) {
        if ( apiKey == null ) {
            return null;
        }

        ApiKeyResponse.ApiKeyResponseBuilder apiKeyResponse = ApiKeyResponse.builder();

        apiKeyResponse.id( apiKey.getId() );
        apiKeyResponse.name( apiKey.getName() );
        apiKeyResponse.description( apiKey.getDescription() );
        apiKeyResponse.permission( apiKey.getPermission() );
        apiKeyResponse.status( apiKey.getStatus() );
        apiKeyResponse.createdAt( apiKey.getCreatedAt() );
        apiKeyResponse.expiredAt( apiKey.getExpiredAt() );
        apiKeyResponse.lastUsedAt( apiKey.getLastUsedAt() );

        apiKeyResponse.expired( apiKey.isExpired() );
        apiKeyResponse.valid( apiKey.isValid() );

        return apiKeyResponse.build();
    }
}
