package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.emails.EmailResponse;
import com.mailshop_dragonvu.entity.EmailLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Email Log Mapper
 */
@Mapper(componentModel = "spring")
public interface EmailLogMapper {

    @Mapping(source = "recipientEmail", target = "to")
    @Mapping(source = "emailStatus", target = "status")
    EmailResponse toResponse(EmailLog emailLog);
}
