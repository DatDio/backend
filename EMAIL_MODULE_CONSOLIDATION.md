# Email Module Consolidation - Changelog

## Changes Made

### 1. Package Structure Consolidation
**Before:**
- `com.mailshop.app.entity.EmailLog`
- `com.mailshop.app.service.EmailService`
- `com.mailshop.app.service.impl.EmailServiceImpl`
- `com.mailshop.app.controller.EmailController`
- `com.mailshop.app.dto.email.*`
- `com.mailshop.app.repository.EmailLogRepository`
- `com.mailshop.app.mapper.EmailLogMapper`

**After:**
- `com.mymarketplace.entity.EmailLog`
- `com.mymarketplace.service.EmailService`
- `com.mymarketplace.service.impl.EmailServiceImpl`
- `com.mymarketplace.controller.EmailController`
- `com.mymarketplace.dto.request.EmailRequest`
- `com.mymarketplace.dto.response.EmailResponse`
- `com.mymarketplace.repository.EmailLogRepository`
- `com.mymarketplace.mapper.EmailLogMapper`
- `com.mymarketplace.enums.EmailStatus`

### 2. Removed Thymeleaf Dependency
- **Removed from pom.xml:** `spring-boot-starter-thymeleaf`
- **Reason:** Frontend sử dụng Angular, không cần server-side HTML templates

### 3. Deleted HTML Email Templates
Đã xóa tất cả 8 HTML templates trong `resources/templates/`:
- ❌ welcome-email.html
- ❌ order-confirmation.html
- ❌ order-status-update.html
- ❌ invoice.html
- ❌ invoice-overdue.html
- ❌ payment-confirmation.html
- ❌ payment-failed.html
- ❌ password-reset.html

### 4. Simplified Email Service Implementation
**Changes in EmailServiceImpl:**
- ✅ Removed `SpringTemplateEngine` dependency
- ✅ Removed Thymeleaf template processing
- ✅ Now sends **plain text emails only** using `JavaMailSender`
- ✅ Email body được format bằng `String.format()` thay vì Thymeleaf
- ✅ Giữ nguyên tất cả business logic (send, retry, tracking)

### 5. Created Database Migration
**New file:** `V3__Create_Email_Logs_Table.sql`
- Tạo bảng `EMAIL_LOGS` với đầy đủ columns
- Tạo sequence `EMAIL_LOGS_SEQ`
- Tạo indexes cho performance
- Foreign keys đến User, Order, Invoice, Payment

### 6. Error Codes
Email error codes đã có sẵn trong `ErrorCode.java`:
- `EMAIL_SEND_FAILED` (8000)
- `EMAIL_TEMPLATE_NOT_FOUND` (8001)
- `EMAIL_INVALID_FORMAT` (8002)
- `EMAIL_NOT_FOUND` (8003)
- `EMAIL_INVALID_STATUS` (8004)

## Email Module Features

### Entity
- **EmailLog** - Tracks all sent emails
  - Status: PENDING, SENT, FAILED
  - Retry mechanism (max 3 retries)
  - Links to User, Order, Invoice, Payment
  - CC/BCC support

### Service Methods
1. `sendEmail(EmailRequest)` - Send plain text email
2. `sendWelcomeEmail(User)` - Welcome new user
3. `sendOrderConfirmationEmail(Order)` - Order placed
4. `sendOrderStatusUpdateEmail(Order)` - Order status changed
5. `sendInvoiceEmail(Invoice)` - Send invoice
6. `sendInvoiceOverdueReminderEmail(Invoice)` - Overdue reminder
7. `sendPaymentConfirmationEmail(Payment)` - Payment success
8. `sendPaymentFailedEmail(Payment)` - Payment failed
9. `sendPasswordResetEmail(User, token)` - Password reset link
10. `retryFailedEmails()` - Scheduled retry (every hour)
11. `getAllEmailLogs(Pageable)` - Admin view all emails
12. `getEmailLogsByStatus(status)` - Filter by status
13. `getEmailLogsByUserId(userId)` - User's emails
14. `getEmailLogById(id)` - Get email details

### REST API Endpoints
**Base URL:** `/api/emails`

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | `/send` | Send custom email | ADMIN |
| GET | `/` | Get all email logs | ADMIN |
| GET | `/{id}` | Get email log by ID | ADMIN |
| GET | `/status/{status}` | Filter by status | ADMIN |
| GET | `/user/{userId}` | User's emails | ADMIN |
| POST | `/retry-failed` | Manual retry failed | ADMIN |

### Configuration
**application.yml:**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

app:
  frontend:
    url: http://localhost:4200
  email:
    max-retries: 3
```

### Auto-Retry Mechanism
- **Scheduled Task:** Runs every hour (`@Scheduled`)
- **Logic:** Find FAILED emails with `retryCount < maxRetries`
- **Max Retries:** 3 (configurable)

## Integration Points

Email service được sử dụng trong:
1. **AuthServiceImpl** - Welcome email khi register
2. **OrderServiceImpl** - Order confirmation & status updates
3. **InvoiceServiceImpl** - Invoice emails & overdue reminders
4. **PaymentServiceImpl** - Payment confirmations & failed notifications

## Benefits of This Consolidation

✅ **Single Package Structure** - Tất cả code trong `com.mymarketplace`  
✅ **Simplified Dependencies** - Không cần Thymeleaf  
✅ **Angular Frontend Ready** - Email body chỉ cần plain text, UI do Angular handle  
✅ **Lightweight** - Giảm dependencies, faster startup  
✅ **Maintainable** - Code gọn hơn, dễ maintain hơn  

## Next Steps

1. **Configure SMTP** - Set environment variables:
   ```bash
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

2. **Run Migration** - Execute `V3__Create_Email_Logs_Table.sql`

3. **Test Email Service** - Send test email via `/api/emails/send`

4. **Monitor Logs** - Check scheduled retry task hourly

## Notes

- Email templates giờ được xử lý bởi Angular frontend
- Backend chỉ gửi plain text emails với links đến Angular UI
- Tất cả email logic (formatting, styling) sẽ nằm ở Angular
- Email service vẫn track đầy đủ logs trong database

---
**Date:** 2024  
**Author:** DragonVu Development Team
