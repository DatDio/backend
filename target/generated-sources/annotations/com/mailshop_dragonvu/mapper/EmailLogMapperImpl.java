package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.emails.EmailResponse;
import com.mailshop_dragonvu.entity.EmailLog;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T18:10:42+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class EmailLogMapperImpl implements EmailLogMapper {

    @Override
    public EmailResponse toResponse(EmailLog emailLog) {
        if ( emailLog == null ) {
            return null;
        }

        EmailResponse.EmailResponseBuilder emailResponse = EmailResponse.builder();

        emailResponse.to( emailLog.getRecipientEmail() );
        if ( emailLog.getEmailStatus() != null ) {
            emailResponse.status( emailLog.getEmailStatus().name() );
        }
        emailResponse.id( emailLog.getId() );
        emailResponse.subject( emailLog.getSubject() );
        emailResponse.errorMessage( emailLog.getErrorMessage() );
        emailResponse.sentAt( emailLog.getSentAt() );
        emailResponse.retryCount( emailLog.getRetryCount() );

        return emailResponse.build();
    }
}
