package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.response.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * API Key Mapper
 */
@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    @Mapping(target = "expired", expression = "java(apiKey.isExpired())")
    @Mapping(target = "valid", expression = "java(apiKey.isValid())")
    ApiKeyResponse toResponse(ApiKey apiKey);
}
