package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.emails.EmailCreateDTO;
import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Email Service Interface
 */
public interface EmailService {

    /**
     * Send email
     */
    EmailResponseDTO sendEmail(EmailCreateDTO emailRequest);

    /**
     * Send welcome email to new user
     */
    void sendWelcomeEmail(UserEntity userEntity);

    /**
     * Send order confirmation email
     */
    void sendOrderConfirmationEmail(OrderEntity orderEntity);

    /**
     * Send order status update email
     */
    void sendOrderStatusUpdateEmail(OrderEntity orderEntity);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(UserEntity userEntity, String resetToken);

    /**
     * Retry failed emails
     */
    //void retryFailedEmails();

    /**
     * Get all email logs with pagination
     */
    Page<EmailResponseDTO> getAllEmailLogs(Pageable pageable);

    /**
     * Get email logs by status
     */
    Page<EmailResponseDTO> getEmailLogsByStatus(String status, Pageable pageable);

    /**
     * Get email logs by user
     */
    Page<EmailResponseDTO> getEmailLogsByUserId(Long userId, Pageable pageable);

    /**
     * Get email log by ID
     */
    EmailResponseDTO getEmailLogById(Long id);
}
