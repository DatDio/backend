package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.entity.EmailLogEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-24T12:33:21+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 18.0.2.1 (Oracle Corporation)"
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
        emailResponseDTO.id( emailLogEntity.getId() );
        emailResponseDTO.subject( emailLogEntity.getSubject() );
        emailResponseDTO.errorMessage( emailLogEntity.getErrorMessage() );
        emailResponseDTO.sentAt( emailLogEntity.getSentAt() );
        emailResponseDTO.retryCount( emailLogEntity.getRetryCount() );

        return emailResponseDTO.build();
    }
}
