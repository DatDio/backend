package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.hotmail.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session store for SSE streaming requests
 */
@Component
public class StreamSessionStore {
    
    // Session data storage
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    
    // Session timeout: 5 minutes
    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;
    
    /**
     * Create a new session for check-live-mail
     */
    public String createCheckLiveMailSession(String emailData) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new SessionData("CHECK_LIVE_MAIL", emailData, null, null));
        return sessionId;
    }
    
    /**
     * Create a new session for get-oauth2
     */
    public String createGetOAuth2Session(String emailData) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new SessionData("GET_OAUTH2", emailData, null, null));
        return sessionId;
    }
    
    /**
     * Create a new session for get-code
     */
    public String createGetCodeSession(HotmailGetCodeRequestDTO request) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new SessionData("GET_CODE", request.getEmailData(), request, null));
        return sessionId;
    }
    
    /**
     * Create a new session for Facebook check-live
     */
    public String createFacebookCheckLiveSession(String uidData) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new SessionData("FACEBOOK_CHECK_LIVE", uidData, null, null));
        return sessionId;
    }
    
    /**
     * Create a new session for read-mail
     */
    public String createReadMailSession(ReadMailRequestDTO request) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new SessionData("READ_MAIL", request.getEmailData(), null, request));
        return sessionId;
    }
    
    /**
     * Get session data and remove it (one-time use)
     */
    public SessionData getAndRemove(String sessionId) {
        return sessions.remove(sessionId);
    }
    
    /**
     * Get session data without removing
     */
    public SessionData get(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * Session data holder
     */
    public static class SessionData {
        public final String type;
        public final String emailData;
        public final HotmailGetCodeRequestDTO getCodeRequest;
        public final ReadMailRequestDTO readMailRequest;
        public final long createdAt;
        
        public SessionData(String type, String emailData, HotmailGetCodeRequestDTO getCodeRequest, ReadMailRequestDTO readMailRequest) {
            this.type = type;
            this.emailData = emailData;
            this.getCodeRequest = getCodeRequest;
            this.readMailRequest = readMailRequest;
            this.createdAt = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > SESSION_TIMEOUT_MS;
        }
    }
}

