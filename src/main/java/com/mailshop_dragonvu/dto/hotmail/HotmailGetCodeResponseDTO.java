package com.mailshop_dragonvu.dto.hotmail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Hotmail get code API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotmailGetCodeResponseDTO {

    /**
     * Email address
     */
    private String email;

    /**
     * Extracted code from email
     */
    private String code;

    /**
     * Sender email address
     */
    private String from;

    /**
     * Email subject
     */
    private String subject;

    /**
     * Time when email was received
     */
    private LocalDateTime receivedAt;
}
