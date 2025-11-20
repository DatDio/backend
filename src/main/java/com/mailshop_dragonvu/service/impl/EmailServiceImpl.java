package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.emails.EmailCreateDTO;
import com.mailshop_dragonvu.dto.emails.EmailResponseDTO;
import com.mailshop_dragonvu.entity.EmailLogEntity;
import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.EmailStatusEnum;
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
    public EmailResponseDTO sendEmail(EmailCreateDTO emailRequest) {
        log.info("Sending email to: {}", emailRequest.getTo());

        EmailLogEntity emailLogEntity = EmailLogEntity.builder()
                .recipientEmail(emailRequest.getTo())
                .subject(emailRequest.getSubject())
                .body(emailRequest.getBody())
                .emailStatus(EmailStatusEnum.PENDING)
                .retryCount(0)
                .build();

        try {
            sendEmailInternal(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
            emailLogEntity.setEmailStatus(EmailStatusEnum.SENT);
            emailLogEntity.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully to: {}", emailRequest.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", emailRequest.getTo(), e);
            emailLogEntity.setEmailStatus(EmailStatusEnum.FAILED);
            emailLogEntity.setErrorMessage(e.getMessage());
        }

        emailLogEntity = emailLogRepository.save(emailLogEntity);
        return emailLogMapper.toResponse(emailLogEntity);
    }

    @Override
    @Async
    public void sendWelcomeEmail(UserEntity userEntity) {
        log.info("Sending welcome email to: {}", userEntity.getEmail());

        String subject = "Welcome to MailShop DragonVu!";
        String body = buildWelcomeEmailBody(userEntity);

        EmailCreateDTO emailRequest = EmailCreateDTO.builder()
                .to(userEntity.getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(OrderEntity orderEntity) {
        log.info("Sending orderEntity confirmation email for orderEntity: {}", orderEntity.getOrderNumber());

        String subject = "Order Confirmation - " + orderEntity.getOrderNumber();
        String body = buildOrderConfirmationEmailBody(orderEntity);

        EmailCreateDTO emailRequest = EmailCreateDTO.builder()
                .to(orderEntity.getUser().getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendOrderStatusUpdateEmail(OrderEntity orderEntity) {
        log.info("Sending orderEntity status update email for orderEntity: {}", orderEntity.getOrderNumber());

        String subject = "Order Status Update - " + orderEntity.getOrderNumber();
        String body = buildOrderStatusUpdateEmailBody(orderEntity);

        EmailCreateDTO emailRequest = EmailCreateDTO.builder()
                .to(orderEntity.getUser().getEmail())
                .subject(subject)
                .body(body)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(UserEntity userEntity, String resetToken) {
        log.info("Sending password reset email to: {}", userEntity.getEmail());

        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "Password Reset Request";
        String body = buildPasswordResetEmailBody(userEntity, resetUrl);

        EmailCreateDTO emailRequest = EmailCreateDTO.builder()
                .to(userEntity.getEmail())
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
    public Page<EmailResponseDTO> getAllEmailLogs(Pageable pageable) {
        return emailLogRepository.findAll(pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    public Page<EmailResponseDTO> getEmailLogsByStatus(String status, Pageable pageable) {
        EmailStatusEnum emailStatus = EmailStatusEnum.valueOf(status.toUpperCase());
        return emailLogRepository.findByEmailStatus(emailStatus, pageable)
                .map(emailLogMapper::toResponse);
    }


    @Override
    public Page<EmailResponseDTO> getEmailLogsByUserId(Long userId, Pageable pageable) {
        return emailLogRepository.findByUserId(userId, pageable)
                .map(emailLogMapper::toResponse);
    }

    @Override
    public EmailResponseDTO getEmailLogById(Long id) {
        EmailLogEntity emailLogEntity = emailLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_LOG_NOT_FOUND));
        return emailLogMapper.toResponse(emailLogEntity);
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

    private String buildWelcomeEmailBody(UserEntity userEntity) {
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
                """.formatted(userEntity.getFullName());
    }

    private String buildOrderConfirmationEmailBody(OrderEntity orderEntity) {
        return """
                <html>
                <body>
                    <h2>Order Confirmation</h2>
                    <p>Hi %s,</p>
                    <p>Your orderEntity <strong>%s</strong> has been confirmed!</p>
                    <p>Order Details:</p>
                    <ul>
                        <li>Order Number: %s</li>
                        <li>Total Amount: %s VND</li>
                        <li>Status: %s</li>
                    </ul>
                    <p>You can view your orderEntity details in your account dashboard.</p>
                    <p>Best regards,<br>MailShop DragonVu Team</p>
                </body>
                </html>
                """.formatted(
                orderEntity.getUser().getFullName(),
                orderEntity.getOrderNumber(),
                orderEntity.getOrderNumber(),
                orderEntity.getFinalAmount(),
                orderEntity.getOrderStatus()
        );
    }

    private String buildOrderStatusUpdateEmailBody(OrderEntity orderEntity) {
        return """
                <html>
                <body>
                    <h2>Order Status Update</h2>
                    <p>Hi %s,</p>
                    <p>Your orderEntity <strong>%s</strong> status has been updated to: <strong>%s</strong></p>
                    <p>You can view your orderEntity details in your account dashboard.</p>
                    <p>Best regards,<br>MailShop DragonVu Team</p>
                </body>
                </html>
                """.formatted(
                orderEntity.getUser().getFullName(),
                orderEntity.getOrderNumber(),
                orderEntity.getOrderStatus()
        );
    }

    private String buildPasswordResetEmailBody(UserEntity userEntity, String resetUrl) {
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
                """.formatted(userEntity.getFullName(), resetUrl);
    }
}
