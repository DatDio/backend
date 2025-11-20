package com.mailshop_dragonvu.dto.emails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCreateDTO {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Email body is required")
    private String body;

    /**
     * Optional CC recipients
     */
    private String[] cc;

    /**
     * Optional BCC recipients
     */
    private String[] bcc;
}
