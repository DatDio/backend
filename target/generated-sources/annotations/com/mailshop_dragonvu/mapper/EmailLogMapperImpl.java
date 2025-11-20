package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.entity.EmailLogEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-20T23:43:41+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class EmailLogMapperImpl implements EmailLogMapper {

    @Override
    public EmailResponseDTO toResponse(EmailLogEntity emailLogEntity) {
        if ( emailLogEntity == null ) {
            return null;
        }

        EmailResponseDTO.EmailResponseDTOBuilder emailResponseDTO = EmailResponseDTO.builder();

        emailResponseDTO.to( emailLogEntity.getRecipientEmail() );
        emailResponseDTO.errorMessage( emailLogEntity.getErrorMessage() );
        emailResponseDTO.id( emailLogEntity.getId() );
        emailResponseDTO.retryCount( emailLogEntity.getRetryCount() );
        emailResponseDTO.sentAt( emailLogEntity.getSentAt() );
        emailResponseDTO.subject( emailLogEntity.getSubject() );

        return emailResponseDTO.build();
    }
}
