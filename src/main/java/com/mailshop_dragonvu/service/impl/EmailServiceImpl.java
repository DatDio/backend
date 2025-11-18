package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.emails.EmailRequest;
import com.mailshop_dragonvu.dto.emails.EmailResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final EmailLogMapper emailLogMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.max-retries:3}")
    private int maxRetries;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async
    @Transactional
    public EmailResponse sendEmail(EmailRequest emailRequest) {
        log.info("Sending email to: {}", emailRequest.getTo());

        EmailLog emailLog = EmailLog.builder()
                .recipientEmail(emailRequest.getTo())
                .subject(emailRequest.getSubject())
                .body(emailRequest.getBody())
                .status("PENDING")
                .retryCount(0)
                .build();

        try {
            sendEmailInternal(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
            emailLog.setStatus("SENT");
            emailLog.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully to: {}", emailRequest.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", emailRequest.getTo(), e);
            emailLog.setStatus("FAILED");
            emailLog.setErrorMessage(e.getMessage());
        }

        emailLog = emailLogRepository.save(emailLog);
        return emailLogMapper.toResponse(emailLog);
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());

        String subject = "Welcome to MailShop DragonVu!";
        String body = buildWelcomeEmailBody(user);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        log.info("Sending order confirmation email for order: {}", order.getOrderNumber());

        String subject = "Order Confirmation - " + order.getOrderNumber();
        String body = buildOrderConfirmationEmailBody(order);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(order.getUser().getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        log.info("Sending order status update email for order: {}", order.getOrderNumber());

        String subject = "Order Status Update - " + order.getOrderNumber();
        String body = buildOrderStatusUpdateEmailBody(order);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(order.getUser().getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to: {}", user.getEmail());

        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "Password Reset Request";
        String body = buildPasswordResetEmailBody(user, resetUrl);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

//    @Override
//    @Transactional
//    public void retryFailedEmails() {
//        log.info("Retrying failed emails...");
//
//        List<EmailLog> failedEmails = emailLogRepository.findByStatusAndRetryCountLessThan("FAILED", maxRetries);
//
//        for (EmailLog emailLog : failedEmails) {
//            try {
//                sendEmailInternal(emailLog.getToEmail(), emailLog.getSubject(), emailLog.getBody());
//                emailLog.setStatus("SENT");
//                emailLog.setSentAt(LocalDateTime.now());
//                log.info("Retry successful for email ID: {}", emailLog.getId());
//            } catch (Exception e) {
//                emailLog.setRetryCount(emailLog.getRetryCount() + 1);
//                emailLog.setErrorMessage(e.getMessage());
//                log.error("Retry failed for email ID: {}", emailLog.getId(), e);
//            }
//            emailLogRepository.save(emailLog);
//        }
//    }

    @Override
    public Page<EmailResponse> getAllEmailLogs(Pageable pageable) {
        return emailLogRepository.findAll(pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    public Page<EmailResponse> getEmailLogsByStatus(String status, Pageable pageable) {
        EmailStatus emailStatus = EmailStatus.valueOf(status.toUpperCase());
        return emailLogRepository.findByEmailStatus(emailStatus, pageable)
                .map(emailLogMapper::toResponse);
    }


    @Override
    public Page<EmailResponse> getEmailLogsByUserId(Long userId, Pageable pageable) {
        return emailLogRepository.findByUserId(userId, pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    public EmailResponse getEmailLogById(Long id) {
        EmailLog emailLog = emailLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_LOG_NOT_FOUND));
        return emailLogMapper.toResponse(emailLog);
    }

    private void sendEmailInternal(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    private String buildWelcomeEmailBody(User user) {
        return """
                <html>
                <body>
                    <h2>Welcome to MailShop DragonVu!</h2>
                    <p>Hi %s,</p>
                    <p>Thank you for registering with us. We're excited to have you on board!</p>
                    <p>You can now browse and purchase mail accounts from our platform.</p>
                    <p>Best regards,<br>MailShop DragonVu Team</p>
                </body>
                </html>
                """.formatted(user.getFullName());
    }

    private String buildOrderConfirmationEmailBody(Order order) {
        return """
                <html>
                <body>
                    <h2>Order Confirmation</h2>
                    <p>Hi %s,</p>
                    <p>Your order <strong>%s</strong> has been confirmed!</p>
                    <p>Order Details:</p>
                    <ul>
                        <li>Order Number: %s</li>
                        <li>Total Amount: %s VND</li>
                        <li>Status: %s</li>
                    </ul>
                    <p>You can view your order details in your account dashboard.</p>
                    <p>Best regards,<br>MailShop DragonVu Team</p>
                </body>
                </html>
                """.formatted(
                order.getUser().getFullName(),
                order.getOrderNumber(),
                order.getOrderNumber(),
                order.getFinalAmount(),
                order.getOrderStatus()
        );
    }

    private String buildOrderStatusUpdateEmailBody(Order order) {
        return """
                <html>
                <body>
                    <h2>Order Status Update</h2>
                    <p>Hi %s,</p>
                    <p>Your order <strong>%s</strong> status has been updated to: <strong>%s</strong></p>
                    <p>You can view your order details in your account dashboard.</p>
                    <p>Best regards,<br>MailShop DragonVu Team</p>
                </body>
                </html>
                """.formatted(
                order.getUser().getFullName(),
                order.getOrderNumber(),
                order.getOrderStatus()
        );
    }

    private String buildPasswordResetEmailBody(User user, String resetUrl) {
        return """
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hi %s,</p>
                    <p>We received a request to reset your password. Click the link below to reset it:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>This link will expire in 24 hours.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                    <p>Best regards,<br>MailShop DragonVu Team</p>
                </body>
                </html>
                """.formatted(user.getFullName(), resetUrl);
    }
}
