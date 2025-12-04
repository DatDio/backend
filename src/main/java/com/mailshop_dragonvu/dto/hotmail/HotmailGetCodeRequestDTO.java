package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting code from Hotmail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotmailGetCodeRequestDTO {

    /**
     * Email data in format: email|password|refresh_token|client_id
     */
    private String emailData;

    /**
     * Type of get: Pop3 or Oauth2
     */
    @Builder.Default
    private String getType = "Oauth2";

    /**
     * Email type filter: Auto, Facebook, Instagram, Twitter, Apple, Tiktok, etc.
     */
    @Builder.Default
    private String emailType = "Auto";
}
