package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.request.EmailRequest;
import com.mailshop_dragonvu.dto.response.EmailResponse;
import com.mailshop_dragonvu.entity.*;
import com.mailshop_dragonvu.enums.EmailStatus;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.EmailLogMapper;
import com.mailshop_dragonvu.repository.EmailLogRepository;
import com.mailshop_dragonvu.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Email Service Implementation
 * Sends plain text emails (Angular frontend handles UI)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final EmailLogMapper emailLogMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.email.max-retries:3}")
    private Integer maxRetries;

    @Override
    @Async
    @Transactional
    public EmailResponse sendEmail(EmailRequest emailRequest) {
        log.info("Sending email to: {}", emailRequest.getTo());

        EmailLog emailLog = EmailLog.builder()
                .recipientEmail(emailRequest.getTo())
                .subject(emailRequest.getSubject())
                .body(emailRequest.getBody())
                .emailStatus(EmailStatus.PENDING)
                .retryCount(0)
                .build();

        if (emailRequest.getCc() != null && emailRequest.getCc().length > 0) {
            emailLog.setCc(String.join(",", emailRequest.getCc()));
        }

        if (emailRequest.getBcc() != null && emailRequest.getBcc().length > 0) {
            emailLog.setBcc(String.join(",", emailRequest.getBcc()));
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getBody(), false); // Plain text only

            if (emailRequest.getCc() != null && emailRequest.getCc().length > 0) {
                helper.setCc(emailRequest.getCc());
            }

            if (emailRequest.getBcc() != null && emailRequest.getBcc().length > 0) {
                helper.setBcc(emailRequest.getBcc());
            }

            mailSender.send(message);

            emailLog.setEmailStatus(EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully to: {}", emailRequest.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}, error: {}", emailRequest.getTo(), e.getMessage());
            emailLog.setEmailStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
        }

        emailLog = emailLogRepository.save(emailLog);
        return emailLogMapper.toResponse(emailLog);
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to user: {}", user.getEmail());

        String body = String.format(
            "Welcome to MailShop!\n\n" +
            "Hello %s,\n\n" +
            "Thank you for joining MailShop. Your account has been successfully created.\n\n" +
            "Email: %s\n\n" +
            "Get started: %s\n\n" +
            "Best regards,\n" +
            "MailShop Team",
            user.getFullName(), user.getEmail(), frontendUrl
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject("Welcome to MailShop!")
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        log.info("Sending order confirmation email for order: {}", order.getOrderNumber());

        StringBuilder itemsList = new StringBuilder();
        order.getOrderItems().forEach(item -> 
            itemsList.append(String.format("- %s x%d: $%.2f\n", 
                item.getProductName(), item.getQuantity(), item.getTotalPrice()))
        );

        String body = String.format(
            "Order Confirmation\n\n" +
            "Hello %s,\n\n" +
            "Your order has been successfully placed.\n\n" +
            "Order Number: %s\n" +
            "Order Date: %s\n\n" +
            "Items:\n%s\n" +
            "Total Amount: $%.2f\n\n" +
            "Shipping Address: %s\n\n" +
            "Track your order: %s/orders/%s\n\n" +
            "Thank you for shopping with us!\n\n" +
            "MailShop Team",
            order.getUser().getFullName(),
            order.getOrderNumber(),
            order.getCreatedAt(),
            itemsList.toString(),
            order.getTotalAmount(),
            order.getShippingAddress(),
            frontendUrl,
            order.getOrderNumber()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(order.getUser().getEmail())
                .subject("Order Confirmation - " + order.getOrderNumber())
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setOrder(order);
            log.setUser(order.getUser());
            emailLogRepository.save(log);
        });
    }

    @Override
    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        log.info("Sending order status update email for order: {}", order.getOrderNumber());

        String body = String.format(
            "Order Status Update\n\n" +
            "Hello %s,\n\n" +
            "Your order status has been updated.\n\n" +
            "Order Number: %s\n" +
            "New Status: %s\n\n" +
            "Track your order: %s/orders/%s\n\n" +
            "MailShop Team",
            order.getUser().getFullName(),
            order.getOrderNumber(),
            order.getOrderStatus().name(),
            frontendUrl,
            order.getOrderNumber()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(order.getUser().getEmail())
                .subject("Order Status Update - " + order.getOrderNumber())
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setOrder(order);
            log.setUser(order.getUser());
            emailLogRepository.save(log);
        });
    }

    @Override
    @Async
    public void sendInvoiceEmail(Invoice invoice) {
        log.info("Sending invoice email for invoice: {}", invoice.getInvoiceNumber());

        String body = String.format(
            "Invoice\n\n" +
            "Dear %s,\n\n" +
            "Please find your invoice details below:\n\n" +
            "Invoice Number: %s\n" +
            "Invoice Date: %s\n" +
            "Due Date: %s\n" +
            "Order Number: %s\n\n" +
            "Total Amount: $%.2f\n" +
            "Amount Paid: $%.2f\n" +
            "Balance Due: $%.2f\n\n" +
            "View invoice: %s/invoices/%s\n\n" +
            "Thank you for your business!\n\n" +
            "MailShop Team",
            invoice.getUser().getFullName(),
            invoice.getInvoiceNumber(),
            invoice.getCreatedAt(),
            invoice.getDueDate(),
            invoice.getOrder().getOrderNumber(),
            invoice.getTotalAmount(),
            invoice.getAmountPaid(),
            invoice.getBalanceDue(),
            frontendUrl,
            invoice.getInvoiceNumber()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(invoice.getUser().getEmail())
                .subject("Invoice - " + invoice.getInvoiceNumber())
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setInvoice(invoice);
            log.setUser(invoice.getUser());
            log.setOrder(invoice.getOrder());
            emailLogRepository.save(log);
        });
    }

    @Override
    @Async
    public void sendInvoiceOverdueReminderEmail(Invoice invoice) {
        log.info("Sending invoice overdue reminder for invoice: {}", invoice.getInvoiceNumber());

        String body = String.format(
            "Invoice Overdue Reminder\n\n" +
            "Dear %s,\n\n" +
            "This is a friendly reminder that your invoice is now overdue.\n\n" +
            "Invoice Number: %s\n" +
            "Due Date: %s\n" +
            "Outstanding Amount: $%.2f\n\n" +
            "Please settle this invoice at your earliest convenience.\n\n" +
            "Pay now: %s/invoices/%s\n\n" +
            "If you have already made the payment, please disregard this email.\n\n" +
            "MailShop Team",
            invoice.getUser().getFullName(),
            invoice.getInvoiceNumber(),
            invoice.getDueDate(),
            invoice.getBalanceDue(),
            frontendUrl,
            invoice.getInvoiceNumber()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(invoice.getUser().getEmail())
                .subject("Invoice Overdue - " + invoice.getInvoiceNumber())
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setInvoice(invoice);
            log.setUser(invoice.getUser());
            emailLogRepository.save(log);
        });
    }

    @Override
    @Async
    public void sendPaymentConfirmationEmail(Payment payment) {
        log.info("Sending payment confirmation email for payment: {}", payment.getPaymentNumber());

        String body = String.format(
            "Payment Confirmation\n\n" +
            "Hello %s,\n\n" +
            "Your payment has been successfully processed.\n\n" +
            "Payment Number: %s\n" +
            "Transaction ID: %s\n" +
            "Amount: $%.2f\n" +
            "Payment Method: %s\n" +
            "Order Number: %s\n\n" +
            "View payment details: %s/payments/%s\n\n" +
            "Thank you for your payment!\n\n" +
            "MailShop Team",
            payment.getUser().getFullName(),
            payment.getPaymentNumber(),
            payment.getTransactionId(),
            payment.getAmount(),
            payment.getPaymentMethod().name(),
            payment.getOrder().getOrderNumber(),
            frontendUrl,
            payment.getPaymentNumber()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(payment.getUser().getEmail())
                .subject("Payment Confirmation - " + payment.getPaymentNumber())
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setPayment(payment);
            log.setUser(payment.getUser());
            log.setOrder(payment.getOrder());
            emailLogRepository.save(log);
        });
    }

    @Override
    @Async
    public void sendPaymentFailedEmail(Payment payment) {
        log.info("Sending payment failed email for payment: {}", payment.getPaymentNumber());

        String body = String.format(
            "Payment Failed\n\n" +
            "Hello %s,\n\n" +
            "Unfortunately, your payment could not be processed.\n\n" +
            "Payment Number: %s\n" +
            "Order Number: %s\n" +
            "Amount: $%.2f\n\n" +
            "What to do next:\n" +
            "- Verify your payment details and try again\n" +
            "- Ensure you have sufficient funds\n" +
            "- Contact your payment provider if the issue persists\n\n" +
            "Try again: %s/orders/%s\n\n" +
            "If you need assistance, please contact our support team.\n\n" +
            "MailShop Team",
            payment.getUser().getFullName(),
            payment.getPaymentNumber(),
            payment.getOrder().getOrderNumber(),
            payment.getAmount(),
            frontendUrl,
            payment.getOrder().getOrderNumber()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(payment.getUser().getEmail())
                .subject("Payment Failed - " + payment.getPaymentNumber())
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setPayment(payment);
            log.setUser(payment.getUser());
            log.setOrder(payment.getOrder());
            emailLogRepository.save(log);
        });
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to user: {}", user.getEmail());

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        String body = String.format(
            "Password Reset Request\n\n" +
            "Hello %s,\n\n" +
            "We received a request to reset your password for your MailShop account.\n\n" +
            "Click the link below to reset your password:\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't request a password reset, please ignore this email.\n\n" +
            "For security reasons, never share this link with anyone.\n\n" +
            "MailShop Team",
            user.getFullName(),
            resetLink
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject("Password Reset Request")
                .body(body)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        
        emailLogRepository.findById(response.getId()).ifPresent(log -> {
            log.setUser(user);
            emailLogRepository.save(log);
        });
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void retryFailedEmails() {
        log.info("Starting retry of failed emails");

        List<EmailLog> failedEmails = emailLogRepository.findFailedEmailsForRetry(maxRetries);
        
        log.info("Found {} failed emails to retry", failedEmails.size());

        for (EmailLog emailLog : failedEmails) {
            try {
                EmailRequest emailRequest = EmailRequest.builder()
                        .to(emailLog.getRecipientEmail())
                        .subject(emailLog.getSubject())
                        .body(emailLog.getBody())
                        .build();

                sendEmail(emailRequest);
                
                emailLog.setRetryCount(emailLog.getRetryCount() + 1);
                emailLogRepository.save(emailLog);
                
            } catch (Exception e) {
                log.error("Failed to retry email {}: {}", emailLog.getId(), e.getMessage());
            }
        }

        log.info("Completed retry of failed emails");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailResponse> getAllEmailLogs(Pageable pageable) {
        return emailLogRepository.findAll(pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailResponse> getEmailLogsByStatus(String status, Pageable pageable) {
        EmailStatus emailStatus;
        try {
            emailStatus = EmailStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_STATUS);
        }

        return emailLogRepository.findByEmailStatus(emailStatus, pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailResponse> getEmailLogsByUserId(Long userId, Pageable pageable) {
        return emailLogRepository.findByUserId(userId, pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailResponse getEmailLogById(Long id) {
        EmailLog emailLog = emailLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
        return emailLogMapper.toResponse(emailLog);
    }
}
