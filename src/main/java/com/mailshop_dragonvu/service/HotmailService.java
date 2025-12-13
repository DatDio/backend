package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.hotmail.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Service interface for Hotmail operations
 */
public interface HotmailService {
    /**
     * Get verification code with SSE streaming (real-time)
     * 
     * @param request contains email credentials and filter options
     * @param emitter SSE emitter for streaming results
     */
    void getCodeStream(HotmailGetCodeRequestDTO request, SseEmitter emitter);


    /**
     * Check live mail with SSE streaming (real-time)
     * 
     * @param emailData email data string
     * @param emitter SSE emitter for streaming results
     */
    void checkLiveMailStream(String emailData, SseEmitter emitter);

    /**
     * Get OAuth2 token with SSE streaming (real-time)
     * 
     * @param emailData email data string
     * @param emitter SSE emitter for streaming results
     */
    void getOAuth2Stream(String emailData, SseEmitter emitter);

    /**
     * Read mailbox with SSE streaming (real-time)
     * 
     * @param request contains email credentials and options
     * @param emitter SSE emitter for streaming results
     */
    void readMailStream(ReadMailRequestDTO request, SseEmitter emitter);
}
