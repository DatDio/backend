package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.request.EmailRequest;
import com.mailshop_dragonvu.dto.response.EmailResponse;
import com.mailshop_dragonvu.service.EmailService;
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
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Management", description = "Email operations and email log management")
@SecurityRequirement(name = "Bearer Authentication")
public class EmailController {

    private final EmailService emailService;

    /**
     * Send email (Admin only)
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send email", description = "Send plain text email (Admin only)")
    public ResponseEntity<ApiResponse<EmailResponse>> sendEmail(
            @Valid @RequestBody EmailRequest emailRequest) {
        log.info("Sending email to: {}", emailRequest.getTo());
        EmailResponse response = emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all email logs (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all email logs", description = "Get all email logs with pagination (Admin only)")
    public ResponseEntity<ApiResponse<Page<EmailResponse>>> getAllEmailLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EmailResponse> emails = emailService.getAllEmailLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    /**
     * Get email log by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get email log by ID", description = "Get email log details by ID (Admin only)")
    public ResponseEntity<ApiResponse<EmailResponse>> getEmailLogById(@PathVariable Long id) {
        log.info("Getting email log with ID: {}", id);
        EmailResponse response = emailService.getEmailLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get email logs by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get email logs by status", description = "Get email logs filtered by status (Admin only)")
    public ResponseEntity<ApiResponse<Page<EmailResponse>>> getEmailLogsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<EmailResponse> emails = emailService.getEmailLogsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    /**
     * Get email logs by user (Admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get email logs by user", description = "Get all emails sent to a specific user (Admin only)")
    public ResponseEntity<ApiResponse<Page<EmailResponse>>> getEmailLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<EmailResponse> emails = emailService.getEmailLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    /**
     * Manually retry failed emails (Admin only)
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retry failed emails", description = "Manually trigger retry of failed emails (Admin only)")
    public ResponseEntity<ApiResponse<String>> retryFailedEmails() {
        log.info("Manually triggering retry of failed emails");
        emailService.retryFailedEmails();
        return ResponseEntity.ok(ApiResponse.success("Failed emails retry initiated"));
    }
}
