package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.entity.EmailLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Email Log Mapper
 */
@Mapper(componentModel = "spring")
public interface EmailLogMapper {

    @Mapping(source = "recipientEmail", target = "to")
    EmailResponseDTO toResponse(EmailLogEntity emailLogEntity);
}
