# MailShop DragonVu - Backend Refactoring

## Các Thay Đổi Lớn

### 1. ✅ Đổi Tên Package
**Từ:** `com.mymarketplace` → **Sang:** `com.mailshop_dragonvu`

Toàn bộ codebase đã được refactor sang package mới:
- Entity, Repository, Service, Controller
- DTO, Mapper, Config, Exception
- Security, Util classes

**POM.xml:**
```xml
<groupId>com.mailshop_dragonvu</groupId>
<artifactId>mailshop-backend</artifactId>
<name>MailShop DragonVu Backend</name>
```

---

### 2. ✅ Hệ Thống Ví & Giao Dịch (Wallet System)

#### **Wallet Entity**
- `BALANCE` - Số dư hiện tại
- `TOTAL_DEPOSITED` - Tổng đã nạp
- `TOTAL_SPENT` - Tổng đã chi
- `IS_LOCKED` - Trạng thái khóa ví
- `LOCK_REASON` - Lý do khóa

**Tự động tạo ví khi user đăng ký** (trigger trong database)

#### **Transaction Entity**
- `TRANSACTION_CODE` - Mã giao dịch unique
- `TYPE` - DEPOSIT, PURCHASE, REFUND, ADMIN_ADJUST
- `AMOUNT` - Số tiền giao dịch
- `STATUS` - PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, REFUNDED
- `BALANCE_BEFORE/AFTER` - Số dư trước/sau giao dịch
- `PAYOS_ORDER_CODE` - Mã đơn PayOS
- `IP_ADDRESS` - IP người dùng (chống DDoS)
- `USER_AGENT` - Browser info
- `PAYMENT_REFERENCE` - Mã tham chiếu thanh toán

---

### 3. ✅ Tích Hợp PayOS Payment Gateway

#### **PayOS Service**
- `createPaymentLink()` - Tạo link thanh toán + QR code
- `getPaymentStatus()` - Kiểm tra trạng thái thanh toán
- `verifyWebhookSignature()` - Xác thực webhook từ PayOS
- `cancelPayment()` - Hủy thanh toán

#### **Quy Trình Nạp Tiền**
1. User nhập số tiền muốn nạp ở FE
2. Backend tạo transaction (PENDING) và gọi PayOS API
3. PayOS trả về QR code + payment link
4. User scan QR hoặc click link để thanh toán
5. PayOS gửi webhook về backend khi thanh toán thành công
6. Backend cập nhật số dư ví (dùng pessimistic lock)
7. Transaction status → SUCCESS

**Configuration (application.yml):**
```yaml
payos:
  api:
    url: https://api-merchant.payos.vn
  client:
    id: ${PAYOS_CLIENT_ID}
  api:
    key: ${PAYOS_API_KEY}
  checksum:
    key: ${PAYOS_CHECKSUM_KEY}
```

---

### 4. ✅ Biện Pháp Bảo Mật - Chống DDoS & Cheat

#### **A. Chống DDoS**

**1. IP Rate Limiting**
```java
checkIpRateLimit(String ipAddress)
// Max 10 transactions/hour per IP
// Index: IDX_TRANSACTION_IP_TIME
```

**2. Pending Transaction Limit**
```java
checkPendingTransactionsLimit(Long userId)
// Max 3 pending transactions per user
// Timeout: 15 minutes
```

**3. Transaction Timeout**
- Transactions tự động fail sau 15 phút nếu không hoàn thành
- Prevent memory leak và zombie transactions

#### **B. Chống Cheat Nạp Tiền**

**1. Amount Validation**
```java
validateDepositAmount(BigDecimal amount)
// Min: 10,000 VND
// Max: 50,000,000 VND
// No decimals allowed (VND không có xu)
```

**2. Duplicate Detection**
```java
checkDuplicateTransactions(userId, amount)
// Detect same amount within 5 minutes
// Index: IDX_TRANSACTION_USER_AMOUNT
```

**3. Pessimistic Locking**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
walletRepository.findByUserIdWithLock(userId)
// Prevent race conditions
// Prevent double spending
```

**4. Webhook Signature Verification**
```java
verifyWebhookSignature(webhookData, signature)
// HMAC SHA256 verification
// Prevent fake webhook attacks
```

**5. Transaction Tracking**
```java
// Lưu IP + User-Agent cho mọi transaction
// Admin có thể trace suspicious activities
// Database index hỗ trợ query nhanh
```

#### **Security Configuration Values**
```yaml
app:
  payment:
    min-amount: 10000  # 10K VND
    max-amount: 50000000  # 50M VND
  
  security:
    max-pending-transactions: 3
    transaction-timeout-minutes: 15
    max-transactions-per-ip-per-hour: 10
```

---

### 5. ✅ Đơn Giản Hóa Order System

#### **Loại Bỏ Shipping (Web bán tài khoản mail - digital product)**

**Removed Fields:**
- ❌ `shippingAddress`, `shippingCity`, `shippingState`
- ❌ `shippingPostalCode`, `shippingCountry`
- ❌ `phone`, `email` (duplicate with user info)
- ❌ `shippedDate`, `deliveredDate`
- ❌ `taxAmount` (simplified)

**Kept Fields:**
- ✅ `orderNumber` - Mã đơn hàng
- ✅ `totalAmount`, `discountAmount`, `finalAmount`
- ✅ `orderStatus` - PENDING, PAID, COMPLETED, CANCELLED, REFUNDED
- ✅ `notes` - Ghi chú
- ✅ `completedDate`, `cancelledDate`

#### **New Order Status Flow**
```
PENDING → PAID → COMPLETED (instant delivery)
         ↓
      CANCELLED → REFUNDED
```

**Digital Product = Instant Delivery:**
- Khi thanh toán thành công → Order status = PAID
- Hệ thống tự động giao tài khoản → Order status = COMPLETED
- Không cần shipping, delivery

---

### 6. ✅ REST API Endpoints

#### **Wallet Endpoints**
```
GET    /api/wallet/me                  - Get my wallet
POST   /api/wallet/deposit             - Create deposit (get QR)
POST   /api/wallet/payos/webhook       - PayOS webhook callback
GET    /api/wallet/transactions        - My transaction history
GET    /api/wallet/transactions/{code} - Get transaction detail

# Admin endpoints
GET    /api/wallet/admin/users/{userId}           - Get user wallet
POST   /api/wallet/admin/users/{userId}/adjust    - Adjust balance
POST   /api/wallet/admin/users/{userId}/lock      - Lock wallet
POST   /api/wallet/admin/users/{userId}/unlock    - Unlock wallet
```

#### **Request/Response Examples**

**Deposit Request:**
```json
{
  "amount": 100000,
  "description": "Nạp tiền vào ví",
  "returnUrl": "http://localhost:4200/payment/success",
  "cancelUrl": "http://localhost:4200/payment/cancel"
}
```

**PayOS Response:**
```json
{
  "transactionCode": "TXN1234567890",
  "orderCode": 1234567890,
  "paymentUrl": "https://pay.payos.vn/...",
  "qrCode": "data:image/png;base64,...",
  "checkoutUrl": "https://checkout.payos.vn/...",
  "amount": "100000",
  "status": "PENDING"
}
```

**Wallet Response:**
```json
{
  "id": 1,
  "userId": 123,
  "balance": 500000,
  "totalDeposited": 1000000,
  "totalSpent": 500000,
  "isLocked": false
}
```

---

### 7. ✅ Database Migrations

#### **V4__Create_Wallets_And_Transactions.sql**
```sql
-- WALLETS table
-- TRANSACTIONS table with security indexes
-- Trigger: Auto-create wallet for new users
-- Indexes for DDoS protection:
  - IDX_TRANSACTION_IP_TIME (IP rate limiting)
  - IDX_TRANSACTION_USER_AMOUNT (duplicate detection)
  - IDX_TRANSACTION_PENDING (pending limit check)
```

**Security Indexes:**
- **IP Rate Limiting:** Query transactions by IP in last hour
- **Duplicate Detection:** Find same amount + user in last 5 minutes
- **Pending Check:** Count pending transactions per user

---

### 8. ✅ Error Codes

**Wallet & Transaction Errors (10000-10999):**
```java
WALLET_NOT_FOUND("10000")
WALLET_ALREADY_EXISTS("10001")
WALLET_LOCKED("10002")
INSUFFICIENT_BALANCE("10003")
TRANSACTION_NOT_FOUND("10004")
TRANSACTION_ALREADY_PROCESSED("10005")
DEPOSIT_AMOUNT_TOO_LOW("10006")
DEPOSIT_AMOUNT_TOO_HIGH("10007")
INVALID_AMOUNT_FORMAT("10008")
TOO_MANY_PENDING_TRANSACTIONS("10009")
DUPLICATE_TRANSACTION("10010")
TRANSACTION_TIMEOUT("10011")
```

**Security Errors (10100-10199):**
```java
RATE_LIMIT_EXCEEDED("10100")
SUSPICIOUS_ACTIVITY("10101")
IP_BLOCKED("10102")
```

---

### 9. ✅ Utilities

#### **SecurityUtils**
```java
getClientIp(HttpServletRequest request)
// Extract IP from X-Forwarded-For, Proxy headers
// Handle proxy chains and load balancers
```

---

### 10. 📊 Architecture Flow

```
┌─────────────┐
│   Angular   │
│   Frontend  │
└──────┬──────┘
       │
       │ 1. POST /api/wallet/deposit
       │    { amount: 100000 }
       │
       ▼
┌─────────────────────────────────┐
│     WalletController            │
│  - Get IP Address               │
│  - Call WalletService           │
└────────┬────────────────────────┘
         │
         │ 2. createDepositTransaction()
         │
         ▼
┌──────────────────────────────────┐
│     WalletServiceImpl            │
│  ✓ Validate amount               │
│  ✓ Check pending limit           │
│  ✓ Check IP rate limit           │
│  ✓ Check duplicates              │
│  ✓ Create Transaction (PENDING)  │
│  ✓ Call PayOS                    │
└────────┬─────────────────────────┘
         │
         │ 3. createPaymentLink()
         │
         ▼
┌──────────────────────────────────┐
│     PayOSServiceImpl             │
│  - Generate checksum             │
│  - Call PayOS API                │
│  - Return QR + payment link      │
└────────┬─────────────────────────┘
         │
         │ 4. Return QR to FE
         │
         ▼
┌─────────────┐
│   User scans│
│   QR code   │
└──────┬──────┘
       │
       │ 5. PayOS webhook
       │
       ▼
┌──────────────────────────────────┐
│   POST /api/wallet/payos/webhook │
│   { orderCode, status }          │
└────────┬─────────────────────────┘
         │
         │ 6. processPayOSCallback()
         │
         ▼
┌──────────────────────────────────┐
│     WalletServiceImpl            │
│  ✓ Find transaction              │
│  ✓ Check timeout                 │
│  ✓ Lock wallet (pessimistic)     │
│  ✓ Add balance                   │
│  ✓ Update transaction → SUCCESS  │
└──────────────────────────────────┘
```

---

### 11. 🔒 Security Features Summary

| Feature | Implementation | Purpose |
|---------|----------------|---------|
| IP Rate Limiting | Max 10 tx/hour per IP | Prevent DDoS |
| Pending Limit | Max 3 pending tx/user | Prevent spam |
| Transaction Timeout | 15 minutes auto-fail | Clean up zombies |
| Amount Validation | Min/Max limits | Prevent invalid amounts |
| Duplicate Detection | Same amount in 5 min | Prevent double charge |
| Pessimistic Lock | Database row lock | Prevent race conditions |
| Webhook Verification | HMAC SHA256 | Prevent fake webhooks |
| IP Tracking | Store IP + User-Agent | Fraud detection |
| Database Indexes | Optimized queries | Fast security checks |

---

### 12. 🚀 Deployment Checklist

#### **Environment Variables Required:**
```bash
# Database
DB_USERNAME=mailshop_user
DB_PASSWORD=mailshop_pass

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-secret-key-here

# PayOS (REQUIRED)
PAYOS_CLIENT_ID=your-client-id
PAYOS_API_KEY=your-api-key
PAYOS_CHECKSUM_KEY=your-checksum-key

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Frontend URL
FRONTEND_URL=http://localhost:4200
```

#### **Database Setup:**
1. Create Oracle database user: `mailshop_user`
2. Run migrations: V1 → V2 → V3 → V4
3. Verify wallet auto-creation trigger

#### **PayOS Setup:**
1. Register at https://payos.vn
2. Get API credentials (Client ID, API Key, Checksum Key)
3. Configure webhook URL: `https://yourdomain.com/api/wallet/payos/webhook`
4. Test with sandbox environment first

#### **Security Configuration:**
```yaml
app:
  security:
    max-pending-transactions: 3
    transaction-timeout-minutes: 15
    max-transactions-per-ip-per-hour: 10
```

---

### 13. 📝 Testing

#### **Test Deposit Flow:**
1. POST `/api/wallet/deposit` với amount = 100000
2. Verify response có QR code và payment URL
3. Scan QR bằng banking app (sandbox)
4. Verify webhook được gọi
5. Check wallet balance updated

#### **Test Anti-DDoS:**
1. Tạo 3 pending transactions → OK
2. Tạo transaction thứ 4 → Error: TOO_MANY_PENDING_TRANSACTIONS
3. Tạo 10 transactions từ 1 IP trong 1 hour → OK
4. Transaction thứ 11 → Error: RATE_LIMIT_EXCEEDED

#### **Test Anti-Cheat:**
1. Deposit amount < 10000 → Error: DEPOSIT_AMOUNT_TOO_LOW
2. Deposit amount > 50M → Error: DEPOSIT_AMOUNT_TOO_HIGH
3. Deposit 100000 hai lần trong 5 phút → Error: DUPLICATE_TRANSACTION
4. Transaction timeout sau 15 phút → Auto-failed

---

### 14. 📚 References

- **PayOS Documentation:** https://docs.payos.vn
- **Spring Boot Security:** https://spring.io/guides/gs/securing-web
- **JPA Pessimistic Locking:** https://www.baeldung.com/jpa-pessimistic-locking
- **Rate Limiting Patterns:** https://cloud.google.com/architecture/rate-limiting-strategies

---

### 15. 🎯 Next Steps (Frontend Integration)

#### **Frontend Cần Làm:**
1. **Wallet Page:**
   - Hiển thị số dư, total deposited, total spent
   - Form nhập số tiền nạp
   - Validation: min 10K, max 50M, integer only

2. **Deposit Flow:**
   - Call API `/api/wallet/deposit`
   - Show QR code modal (received from backend)
   - Show payment URL button
   - Polling or WebSocket để check transaction status

3. **Transaction History:**
   - Call API `/api/wallet/transactions`
   - Display table with: code, type, amount, status, date
   - Filter by type/status

4. **Order Flow:**
   - Check wallet balance before purchase
   - If insufficient → redirect to deposit page
   - If sufficient → deduct from wallet
   - Instant delivery of mail account

5. **Error Handling:**
   - Rate limit exceeded → Show "Please wait" message
   - Duplicate transaction → Show "You have pending transaction"
   - Amount validation → Show error in form

---

## Tóm Tắt Công Việc Đã Hoàn Thành

✅ Đổi package name: `com.mymarketplace` → `com.mailshop_dragonvu`  
✅ Tạo Wallet system với auto-create trigger  
✅ Tạo Transaction tracking với đầy đủ security features  
✅ Tích hợp PayOS payment gateway  
✅ Implement chống DDoS: IP rate limiting, pending limit, timeout  
✅ Implement chống cheat: amount validation, duplicate detection, pessimistic lock  
✅ Loại bỏ shipping fields (digital product)  
✅ Đơn giản hóa Order status flow  
✅ Tạo REST API endpoints  
✅ Tạo database migrations với security indexes  
✅ Cập nhật error codes  
✅ Tạo utilities (SecurityUtils, RestTemplate)  
✅ Cập nhật application.yml với PayOS config  

## Status: ✅ COMPLETED
Backend đã sẵn sàng để tích hợp với Angular frontend!
