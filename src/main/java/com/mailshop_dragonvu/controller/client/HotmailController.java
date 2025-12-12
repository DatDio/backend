package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.hotmail.*;
import com.mailshop_dragonvu.service.HotmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Get verification code from Hotmail inbox
     * 
     * @param request contains email credentials
     *                (email|password|refresh_token|client_id)
     * @return list of emails with extracted codes
     */
    @PostMapping("/get-code")
    @Operation(summary = "Get verification code from Hotmail", description = "Read Hotmail inbox and extract verification codes. "
            +
            "Supports Graph API (OAuth2) and IMAP methods. " +
            "Email data format: email|password|refresh_token|client_id")
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
     * Check if email accounts are live
     * 
     * @param request contains email credentials
     *                (email|password|refresh_token|client_id)
     * @return list of emails with live/die status
     */
    @PostMapping("/check-live-mail")
    @Operation(summary = "Check if mail accounts are live", 
            description = "Check if email accounts are valid by verifying OAuth2 token refresh. " +
            "Email data format: email|password|refresh_token|client_id")
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
     * Get OAuth2 access token from refresh token
     * 
     * @param request contains email credentials with refresh token
     *                (email|password|refresh_token|client_id)
     * @return list of results with access tokens
     */
    @PostMapping("/get-oauth2")
    @Operation(summary = "Get OAuth2 access token from refresh token", 
            description = "Get new OAuth2 access tokens using refresh tokens. " +
            "Email data format: email|password|refresh_token|client_id")
    public ResponseEntity<ApiResponse<List<GetOAuth2ResponseDTO>>> getOAuth2Token(
            @RequestBody GetOAuth2RequestDTO request) {

        log.info("Getting OAuth2 tokens for {} lines", 
                request.getEmailData() != null ? request.getEmailData().split("\n").length : 0);

        List<GetOAuth2ResponseDTO> results = hotmailService.getOAuth2Token(request);

        long successCount = results.stream().filter(GetOAuth2ResponseDTO::isSuccess).count();
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Token refresh completed: %d/%d success", successCount, results.size()), results));
    }
}
