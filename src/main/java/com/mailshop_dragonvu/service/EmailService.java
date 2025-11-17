package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.request.EmailRequest;
import com.mailshop_dragonvu.dto.response.EmailResponse;
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
    EmailResponse sendEmail(EmailRequest emailRequest);

    /**
     * Send welcome email to new user
     */
    void sendWelcomeEmail(User user);

    /**
     * Send order confirmation email
     */
    void sendOrderConfirmationEmail(Order order);

    /**
     * Send order status update email
     */
    void sendOrderStatusUpdateEmail(Order order);

    /**
     * Send invoice email
     */
    void sendInvoiceEmail(Invoice invoice);

    /**
     * Send invoice overdue reminder
     */
    void sendInvoiceOverdueReminderEmail(Invoice invoice);

    /**
     * Send payment confirmation email
     */
    void sendPaymentConfirmationEmail(Payment payment);

    /**
     * Send payment failed email
     */
    void sendPaymentFailedEmail(Payment payment);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(User user, String resetToken);

    /**
     * Retry failed emails
     */
    void retryFailedEmails();

    /**
     * Get all email logs with pagination
     */
    Page<EmailResponse> getAllEmailLogs(Pageable pageable);

    /**
     * Get email logs by status
     */
    Page<EmailResponse> getEmailLogsByStatus(String status, Pageable pageable);

    /**
     * Get email logs by user
     */
    Page<EmailResponse> getEmailLogsByUserId(Long userId, Pageable pageable);

    /**
     * Get email log by ID
     */
    EmailResponse getEmailLogById(Long id);
}
