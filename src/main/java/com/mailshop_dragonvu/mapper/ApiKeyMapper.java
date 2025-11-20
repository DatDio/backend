package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.apikeys.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKeyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * API Key Mapper
 */
@Mapper(componentModel = "spring")
public interface ApiKeyMapper {
    ApiKeyResponse toResponse(ApiKeyEntity apiKeyEntity);
}
