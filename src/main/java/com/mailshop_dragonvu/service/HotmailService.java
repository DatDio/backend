package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.hotmail.*;

import java.util.List;

/**
 * Service interface for Hotmail operations
 */
public interface HotmailService {

    /**
     * Get verification code from Hotmail inbox
     * 
     * @param request contains email credentials and filter options
     * @return list of emails with extracted codes
     */
    List<HotmailGetCodeResponseDTO> getCode(HotmailGetCodeRequestDTO request);

    /**
     * Check if email accounts are live by verifying OAuth2 token refresh
     * 
     * @param request contains email credentials
     * @return list of results with live/die status
     */
    List<CheckLiveMailResponseDTO> checkLiveMail(CheckLiveMailRequestDTO request);

    /**
     * Get OAuth2 access token from refresh token
     * 
     * @param request contains email credentials with refresh token
     * @return list of results with access tokens
     */
    List<GetOAuth2ResponseDTO> getOAuth2Token(GetOAuth2RequestDTO request);
}
