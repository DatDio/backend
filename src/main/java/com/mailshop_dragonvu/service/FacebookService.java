package com.mailshop_dragonvu.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service for Facebook operations
 */
public interface FacebookService {
    
    /**
     * Check if Facebook UIDs are live with SSE streaming
     * @param uidData - UIDs separated by newlines
     * @param emitter - SSE emitter for streaming results
     */
    void checkLiveStream(String uidData, SseEmitter emitter);
}
