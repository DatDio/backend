package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.emails.EmailCreateDTO;
import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.service.EmailService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Email Controller - handles email operations
 */
@RestController("adminEmailController")
@RequestMapping("/admin" + Constants.API_PATH.EMAILS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Email Management", description = "Email operations and email log management")
@SecurityRequirement(name = "Bearer Authentication")
public class EmailController {

    private final EmailService emailService;

    /**
     * Send email (Admin only)
     */
    @PostMapping("/send")
    @Operation(summary = "Send email", description = "Send plain text email (Admin only)")
    public ResponseEntity<ApiResponse<EmailResponseDTO>> sendEmail(
            @Valid @RequestBody EmailCreateDTO emailRequest) {
        log.info("Sending email to: {}", emailRequest.getTo());
        EmailResponseDTO response = emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all email logs (Admin only)
     */
    @GetMapping
    @Operation(summary = "Get all email logs", description = "Get all email logs with pagination (Admin only)")
    public ResponseEntity<ApiResponse<Page<EmailResponseDTO>>> getAllEmailLogs(
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
     * Get email log by ID (Admin only)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get email log by ID", description = "Get email log details by ID (Admin only)")
    public ResponseEntity<ApiResponse<EmailResponseDTO>> getEmailLogById(@PathVariable Long id) {
        log.info("Getting email log with ID: {}", id);
        EmailResponseDTO response = emailService.getEmailLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get email logs by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get email logs by status", description = "Get email logs filtered by status (Admin only)")
    public ResponseEntity<ApiResponse<Page<EmailResponseDTO>>> getEmailLogsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<EmailResponseDTO> emails = emailService.getEmailLogsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    /**
     * Get email logs by user (Admin only)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get email logs by user", description = "Get all emails sent to a specific user (Admin only)")
    public ResponseEntity<ApiResponse<Page<EmailResponseDTO>>> getEmailLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<EmailResponseDTO> emails = emailService.getEmailLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    /**
     * Manually retry failed emails (Admin only)
     */
//    @PostMapping("/retry-failed")
//    @Operation(summary = "Retry failed emails", description = "Manually trigger retry of failed emails (Admin only)")
//    public ResponseEntity<ApiResponse<String>> retryFailedEmails() {
//        log.info("Manually triggering retry of failed emails");
//        emailService.retryFailedEmails();
//        return ResponseEntity.ok(ApiResponse.success("Failed emails retry initiated"));
//    }
}
