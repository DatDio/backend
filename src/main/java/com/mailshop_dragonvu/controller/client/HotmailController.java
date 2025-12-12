package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.hotmail.*;
import com.mailshop_dragonvu.service.HotmailService;
import com.mailshop_dragonvu.service.StreamSessionStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Controller for Hotmail operations
 * Public API - no authentication required
 */
@RestController
@RequestMapping("api/v1/hotmail")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hotmail", description = "Hotmail email operations - Get verification codes, check live mail, get OAuth2 tokens")
public class HotmailController {

    private final HotmailService hotmailService;
    private final StreamSessionStore sessionStore;

    // ==================== GET CODE ====================

    @PostMapping("/get-code")
    @Operation(summary = "Get verification code from Hotmail")
    public ResponseEntity<ApiResponse<List<HotmailGetCodeResponseDTO>>> getCode(
            @RequestBody HotmailGetCodeRequestDTO request) {

        log.info("Getting code for email types: {}, get type: {}",
                request.getEmailTypes(), request.getGetType());

        List<HotmailGetCodeResponseDTO> codes = hotmailService.getCode(request);

        if (codes.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No verification codes found", codes));
        }

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d verification code(s)", codes.size()), codes));
    }

    /**
     * Step 1: POST data, get sessionId
     */
    @PostMapping("/get-code/start")
    @Operation(summary = "Start get-code session", description = "Submit email data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startGetCode(@RequestBody HotmailGetCodeRequestDTO request) {
        String sessionId = sessionStore.createGetCodeSession(request);
        log.info("Created get-code session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/get-code/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

    // ==================== CHECK LIVE MAIL ====================

    @PostMapping("/check-live-mail")
    @Operation(summary = "Check if mail accounts are live")
    public ResponseEntity<ApiResponse<List<CheckLiveMailResponseDTO>>> checkLiveMail(
            @RequestBody CheckLiveMailRequestDTO request) {

        log.info("Checking live mail for {} lines", 
                request.getEmailData() != null ? request.getEmailData().split("\n").length : 0);

        List<CheckLiveMailResponseDTO> results = hotmailService.checkLiveMail(request);

        long liveCount = results.stream().filter(CheckLiveMailResponseDTO::isLive).count();
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Check completed: %d/%d live", liveCount, results.size()), results));
    }

    /**
     * Step 1: POST data, get sessionId
     */
    @PostMapping("/check-live-mail/start")
    @Operation(summary = "Start check-live-mail session", description = "Submit email data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startCheckLiveMail(@RequestBody CheckLiveMailRequestDTO request) {
        String sessionId = sessionStore.createCheckLiveMailSession(request.getEmailData());
        log.info("Created check-live-mail session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/check-live-mail/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

    // ==================== GET OAUTH2 ====================

    @PostMapping("/get-oauth2")
    @Operation(summary = "Get OAuth2 access token from refresh token")
    public ResponseEntity<ApiResponse<List<GetOAuth2ResponseDTO>>> getOAuth2Token(
            @RequestBody GetOAuth2RequestDTO request) {

        log.info("Getting OAuth2 tokens for {} lines", 
                request.getEmailData() != null ? request.getEmailData().split("\n").length : 0);

        List<GetOAuth2ResponseDTO> results = hotmailService.getOAuth2Token(request);

        long successCount = results.stream().filter(GetOAuth2ResponseDTO::isSuccess).count();
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Token refresh completed: %d/%d success", successCount, results.size()), results));
    }

    /**
     * Step 1: POST data, get sessionId
     */
    @PostMapping("/get-oauth2/start")
    @Operation(summary = "Start get-oauth2 session", description = "Submit email data and get sessionId for streaming")
    public ResponseEntity<ApiResponse<Map<String, String>>> startGetOAuth2(@RequestBody GetOAuth2RequestDTO request) {
        String sessionId = sessionStore.createGetOAuth2Session(request.getEmailData());
        log.info("Created get-oauth2 session: {}", sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session created", Map.of("sessionId", sessionId)));
    }

    /**
     * Step 2: GET stream with sessionId
     */
    @GetMapping(value = "/get-oauth2/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
}
