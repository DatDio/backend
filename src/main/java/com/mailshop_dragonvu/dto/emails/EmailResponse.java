package com.mailshop_dragonvu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Email Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {

    private Long id;
    private String to;
    private String subject;
    private String status;
    private String errorMessage;
    private LocalDateTime sentAt;
    private Integer retryCount;
}
