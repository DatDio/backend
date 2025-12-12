package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.facebook.FacebookCheckLiveRequestDTO;
import com.mailshop_dragonvu.dto.hotmail.*;
import com.mailshop_dragonvu.service.FacebookService;
import com.mailshop_dragonvu.service.HotmailService;
import com.mailshop_dragonvu.service.StreamSessionStore;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping(Constants.API_PATH.TOOLS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tools", description = "Utility tools - Hotmail operations, Facebook check live")
public class ToolController {

    private final HotmailService hotmailService;
    private final FacebookService facebookService;
    private final StreamSessionStore sessionStore;

    // ==================== HOTMAIL - GET CODE ====================
    
    /**
     * Step 1: POST data, get sessionId
     */
    @PostMapping("/hotmail/get-code/start")
    @Operation(summary = "Start get-code session", description = "Submit email data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startGetCode(@RequestBody HotmailGetCodeRequestDTO request) {
        String sessionId = sessionStore.createGetCodeSession(request);
        log.info("Created get-code session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/hotmail/get-code/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream get-code results")
    public SseEmitter getCodeStream(@RequestParam String sessionId) {
        StreamSessionStore.SessionData session = sessionStore.getAndRemove(sessionId);
        
        if (session == null || session.isExpired()) {
            log.warn("Invalid or expired session: {}", sessionId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
        
        log.info("Starting SSE stream for get-code session: {}", sessionId);
        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout
        hotmailService.getCodeStream(session.getCodeRequest, emitter);
        return emitter;
    }

    // ==================== HOTMAIL - CHECK LIVE MAIL ====================

    /**
     * Step 1: POST data, get sessionId
     */
    @PostMapping("/hotmail/check-live-mail/start")
    @Operation(summary = "Start check-live-mail session", description = "Submit email data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startCheckLiveMail(@RequestBody CheckLiveMailRequestDTO request) {
        String sessionId = sessionStore.createCheckLiveMailSession(request.getEmailData());
        log.info("Created check-live-mail session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/hotmail/check-live-mail/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream check-live-mail results")
    public SseEmitter checkLiveMailStream(@RequestParam String sessionId) {
        StreamSessionStore.SessionData session = sessionStore.getAndRemove(sessionId);
        
        if (session == null || session.isExpired()) {
            log.warn("Invalid or expired session: {}", sessionId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
        
        log.info("Starting SSE stream for check-live-mail session: {}", sessionId);
        SseEmitter emitter = new SseEmitter(300000L);
        hotmailService.checkLiveMailStream(session.emailData, emitter);
        return emitter;
    }

    // ==================== HOTMAIL - GET OAUTH2 ====================
    
    /**
     * Step 1: POST data, get sessionId
     */
    @PostMapping("/hotmail/get-oauth2/start")
    @Operation(summary = "Start get-oauth2 session", description = "Submit email data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startGetOAuth2(@RequestBody GetOAuth2RequestDTO request) {
        String sessionId = sessionStore.createGetOAuth2Session(request.getEmailData());
        log.info("Created get-oauth2 session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/hotmail/get-oauth2/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream get-oauth2 results")
    public SseEmitter getOAuth2Stream(@RequestParam String sessionId) {
        StreamSessionStore.SessionData session = sessionStore.getAndRemove(sessionId);
        
        if (session == null || session.isExpired()) {
            log.warn("Invalid or expired session: {}", sessionId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
        
        log.info("Starting SSE stream for get-oauth2 session: {}", sessionId);
        SseEmitter emitter = new SseEmitter(300000L);
        hotmailService.getOAuth2Stream(session.emailData, emitter);
        return emitter;
    }

    // ==================== FACEBOOK - CHECK LIVE ====================
    
    /**
     * Step 1: POST UID data, get sessionId
     */
    @PostMapping("/facebook/check-live/start")
    @Operation(summary = "Start Facebook check-live session", description = "Submit UID data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startFacebookCheckLive(@RequestBody FacebookCheckLiveRequestDTO request) {
        String sessionId = sessionStore.createFacebookCheckLiveSession(request.getUidData());
        log.info("Created facebook check-live session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/facebook/check-live/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream Facebook check-live results")
    public SseEmitter facebookCheckLiveStream(@RequestParam String sessionId) {
        StreamSessionStore.SessionData session = sessionStore.getAndRemove(sessionId);
        
        if (session == null || session.isExpired()) {
            log.warn("Invalid or expired session: {}", sessionId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
        
        log.info("Starting SSE stream for facebook check-live session: {}", sessionId);
        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout
        facebookService.checkLiveStream(session.emailData, emitter);
        return emitter;
    }
}
