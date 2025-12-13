package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reading mailbox
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadMailRequestDTO {

    /**
     * Email data in format: email|password|refresh_token|client_id
     * Multiple lines supported (one email per line)
     */
    private String emailData;

    /**
     * Number of messages to fetch per email (default: 20)
     */
    @Builder.Default
    private int messageCount = 20;
}
