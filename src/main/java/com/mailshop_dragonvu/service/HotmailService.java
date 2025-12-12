package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.hotmail.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
     * Get verification code with SSE streaming (real-time)
     * 
     * @param request contains email credentials and filter options
     * @param emitter SSE emitter for streaming results
     */
    void getCodeStream(HotmailGetCodeRequestDTO request, SseEmitter emitter);

    /**
     * Check if email accounts are live by verifying OAuth2 token refresh
     * 
     * @param request contains email credentials
     * @return list of results with live/die status
     */
    List<CheckLiveMailResponseDTO> checkLiveMail(CheckLiveMailRequestDTO request);

    /**
     * Check live mail with SSE streaming (real-time)
     * 
     * @param emailData email data string
     * @param emitter SSE emitter for streaming results
     */
    void checkLiveMailStream(String emailData, SseEmitter emitter);

    /**
     * Get OAuth2 access token from refresh token
     * 
     * @param request contains email credentials with refresh token
     * @return list of results with access tokens
     */
    List<GetOAuth2ResponseDTO> getOAuth2Token(GetOAuth2RequestDTO request);

    /**
     * Get OAuth2 token with SSE streaming (real-time)
     * 
     * @param emailData email data string
     * @param emitter SSE emitter for streaming results
     */
    void getOAuth2Stream(String emailData, SseEmitter emitter);
}
