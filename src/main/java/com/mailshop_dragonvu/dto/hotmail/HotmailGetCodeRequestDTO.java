package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * Multiple lines supported (one email per line)
     */
    private String emailData;

    /**
     * Type of get: Graph API or Oauth2
     */
    @Builder.Default
    private String getType = "Oauth2";

    /**
     * Email type filters: Auto, Facebook, Instagram, Twitter, Apple, Tiktok, etc.
     * Supports multiple selections
     */
    @Builder.Default
    private List<String> emailTypes = List.of("Auto");
}

