package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.service.EmailService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Email Controller - handles email operations for clients
 */
@RestController
@RequestMapping(Constants.API_PATH.EMAILS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Management", description = "Email operations")
@SecurityRequirement(name = "Bearer Authentication")
public class EmailController {

    private final EmailService emailService;

    /**
     * Get email logs for current user
     */
    @GetMapping("/my-logs")
    @Operation(summary = "Get my email logs", description = "Get your email logs with pagination")
    public ResponseEntity<ApiResponse<Page<EmailResponseDTO>>> getMyEmailLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EmailResponseDTO> emails = emailService.getAllEmailLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    /**
     * Get email log by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get email log by ID", description = "Get email log details by ID")
    public ResponseEntity<ApiResponse<EmailResponseDTO>> getEmailLogById(@PathVariable Long id) {
        log.info("Getting email log with ID: {}", id);
        EmailResponseDTO response = emailService.getEmailLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
