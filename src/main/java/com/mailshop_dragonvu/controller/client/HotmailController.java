package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.hotmail.HotmailGetCodeRequestDTO;
import com.mailshop_dragonvu.dto.hotmail.HotmailGetCodeResponseDTO;
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
@Tag(name = "Hotmail", description = "Hotmail email operations - Get verification codes")
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
}
