package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for reading mailbox - contains list of emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadMailResponseDTO {

    /**
     * Email address
     */
    private String email;

    /**
     * Password (for reference)
     */
    private String password;

    /**
     * Whether reading was successful
     */
    private boolean success;

    /**
     * Check status for UI display
     */
    private CheckStatus status;

    /**
     * List of email messages
     */
    private List<EmailMessage> messages;

    /**
     * Error message if failed
     */
    private String error;

    /**
     * Total message count in inbox
     */
    private int totalMessages;

    /**
     * Single email message
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailMessage {
        /**
         * Email subject
         */
        private String subject;

        /**
         * Sender email address
         */
        private String from;

        /**
         * Preview of email body (first 200 chars)
         */
        private String preview;

        /**
         * Full HTML body of email
         */
        private String htmlBody;

        /**
         * Received date formatted
         */
        private String date;

        /**
         * Whether email has been read
         */
        private boolean isRead;

        /**
         * Whether email has attachments
         */
        private boolean hasAttachments;
    }
}
