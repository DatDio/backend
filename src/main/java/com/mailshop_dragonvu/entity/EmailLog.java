package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Email Log Entity - tracks all sent emails
 */
@Entity
@Table(name = "EMAIL_LOGS")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog extends BaseEntity {

    @Column(name = "RECIPIENT_EMAIL", nullable = false)
    private String recipientEmail;

    @Column(name = "SUBJECT", nullable = false)
    private String subject;

    @Column(name = "BODY", columnDefinition = "CLOB")
    private String body;

    @Column(name = "EMAIL_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmailStatus emailStatus = EmailStatus.PENDING;

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    @Column(name = "SENT_AT")
    private LocalDateTime sentAt;

    @Column(name = "RETRY_COUNT")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "CC")
    private String cc;

    @Column(name = "BCC")
    private String bcc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private Order order;


    @Column(name = "STATUS", length = 20, nullable = false)
    private String status = "ACTIVE";
}
