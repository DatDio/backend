# MailShop DragonVu - Simplified Architecture

## Tối Giản Hóa Hoàn Toàn ✅

### 📊 Database Schema (Chỉ 7 Bảng Chính)

```
1. USERS              - Người dùng
2. ROLES              - Vai trò
3. PERMISSIONS        - Quyền hạn
4. USER_ROLES         - User-Role mapping
5. ROLE_PERMISSIONS   - Role-Permission mapping
6. WALLETS            - Số dư người dùng
7. TRANSACTIONS       - Lịch sử giao dịch (nạp tiền + mua hàng)
8. ORDERS             - Đơn hàng (tài khoản mail)
9. ORDER_ITEMS        - Chi tiết đơn hàng
10. API_KEYS          - API keys
```

**❌ Đã Loại Bỏ:**
- INVOICES (không cần)
- PAYMENTS (không cần)
- EMAIL_LOGS (giữ đơn giản)

---

## 🎯 Luồng Hoạt Động Đơn Giản

### 1. Nạp Tiền (Deposit)
```
User → Nhập số tiền → PayOS QR Code → Scan & Pay → Tăng số dư ví
```

**Backend Flow:**
```java
POST /api/wallet/deposit
  ↓
1. Validate amount (10K - 50M VND)
2. Check anti-DDoS (max 3 pending, max 10 tx/hour/IP)
3. Check duplicate (same amount in 5 mins)
4. Create Transaction (PENDING)
5. Call PayOS API → Get QR code
6. Return QR to frontend
  ↓
PayOS Webhook Callback
  ↓
7. Find Transaction by orderCode
8. Lock Wallet (pessimistic lock)
9. Update Balance (+amount)
10. Transaction status = SUCCESS
```

### 2. Mua Hàng (Purchase)
```
User → Chọn sản phẩm → Check số dư → Trừ tiền → Giao tài khoản
```

**Backend Flow:**
```java
POST /api/orders
  ↓
1. Check wallet balance (sufficient?)
2. Lock Wallet
3. Deduct balance (-amount)
4. Create Transaction (PURCHASE, SUCCESS)
5. Create Order (PAID)
6. Deliver digital product (mail account)
7. Order status = COMPLETED
```

### 3. Lịch Sử
```
GET /api/wallet/transactions?type=DEPOSIT    - Lịch sử nạp tiền
GET /api/wallet/transactions?type=PURCHASE   - Lịch sử mua hàng
GET /api/orders                               - Đơn hàng đã mua
```

---

## 🔒 Chống DDoS Toàn App

### Global Rate Limiting Filter

**Áp dụng cho TẤT CẢ endpoints:**
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    // 100 requests/minute per IP
    // Tự động block nếu vượt quá
}
```

**Filter Chain Order:**
```
RateLimitingFilter              ← Chạy đầu tiên (chống DDoS)
  ↓
ApiKeyAuthenticationFilter      ← API key auth
  ↓
JwtAuthenticationFilter         ← JWT auth
  ↓
Controllers                     ← Business logic
```

**Config:**
```yaml
app:
  security:
    rate-limit:
      requests-per-minute: 100      # Toàn app
      requests-per-hour: 1000
    max-pending-transactions: 3     # Per user
    max-transactions-per-ip-per-hour: 10  # Payment specific
```

**Tính Năng:**
- ✅ Block IP nếu spam (100 req/min)
- ✅ Return 429 Too Many Requests
- ✅ Cache buckets per IP (Bucket4j library)
- ✅ Skip health check endpoints
- ✅ Auto cleanup cache

---

## 📁 Simplified Entity Structure

### Wallet (Ví tiền)
```java
- balance              // Số dư hiện tại
- totalDeposited       // Tổng đã nạp
- totalSpent           // Tổng đã chi
- isLocked             // Khóa ví (admin)
```

### Transaction (Giao dịch)
```java
- transactionCode      // Mã unique
- type                 // DEPOSIT | PURCHASE | REFUND | ADMIN_ADJUST
- amount               // Số tiền
- balanceBefore/After  // Số dư trước/sau
- status               // PENDING | SUCCESS | FAILED
- payosOrderCode       // Mã PayOS
- ipAddress            // IP người dùng
```

### Order (Đơn hàng)
```java
- orderNumber          // Mã đơn
- orderStatus          // PENDING | PAID | COMPLETED
- totalAmount          // Tổng tiền
- discountAmount       // Giảm giá
- finalAmount          // Thành tiền
- completedDate        // Ngày hoàn thành
```

**❌ Không có:**
- Shipping address (digital product)
- Tax amount (simplified)
- Invoice reference (not needed)
- Payment reference (transaction handles it)

---

## 🛡️ Security Features

### 1. Global Rate Limiting (Toàn App)
```
100 requests/minute per IP
Block tự động nếu spam
```

### 2. Payment-Specific Protection
```
✓ Max 3 pending transactions/user
✓ Max 10 payment transactions/hour/IP
✓ Duplicate detection (5 minutes)
✓ Amount validation (10K-50M VND)
✓ Transaction timeout (15 minutes)
```

### 3. Database-Level Protection
```
✓ Pessimistic locking (prevent race conditions)
✓ Indexes for fast security queries
✓ IP tracking for audit
```

### 4. PayOS Security
```
✓ HMAC SHA256 signature verification
✓ Webhook validation
✓ Checksum generation
```

---

## 📡 API Endpoints (Tối Giản)

### Wallet
```
GET    /api/wallet/me                  - Xem số dư
POST   /api/wallet/deposit             - Nạp tiền (nhận QR)
GET    /api/wallet/transactions        - Lịch sử giao dịch
POST   /api/wallet/payos/webhook       - PayOS callback (internal)
```

### Orders
```
GET    /api/orders                     - Danh sách đơn hàng
POST   /api/orders                     - Tạo đơn (mua hàng)
GET    /api/orders/{id}                - Chi tiết đơn hàng
```

### Admin
```
GET    /api/wallet/admin/users/{id}           - Xem ví user
POST   /api/wallet/admin/users/{id}/adjust    - Điều chỉnh số dư
POST   /api/wallet/admin/users/{id}/lock      - Khóa ví
```

---

## 🗄️ Database Migrations

```sql
V1__Create_Base_Tables.sql              - Users, Roles, Permissions
V2__Create_API_Keys_Table.sql           - API Keys
V3__Create_Email_Logs_Table.sql         - (Kept for future)
V4__Create_Wallets_And_Transactions.sql - Wallets, Transactions
V5__Simplify_Schema.sql                 - Drop Invoice/Payment tables
```

**V5 Migration:**
- DROP INVOICES table
- DROP PAYMENTS table  
- Remove foreign key references
- Clean up unused columns

---

## 📦 Dependencies

**Core:**
- Spring Boot 3.2.0
- Java 17
- Oracle Database
- Redis

**Security:**
- Spring Security
- JWT (jjwt 0.12.3)
- OAuth2 (Google)
- **Bucket4j 8.7.0** (Rate limiting)

**Payment:**
- PayOS (Vietnamese payment gateway)
- RestTemplate for API calls

**Removed:**
- ❌ MoMo SDK
- ❌ PayPal SDK
- ❌ Thymeleaf (no templates needed)

---

## ⚙️ Configuration

### Environment Variables Required:
```bash
# Database
DB_USERNAME=mailshop_user
DB_PASSWORD=mailshop_pass

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-jwt-secret-key

# PayOS (Required)
PAYOS_CLIENT_ID=your-client-id
PAYOS_API_KEY=your-api-key
PAYOS_CHECKSUM_KEY=your-checksum-key

# Optional
FRONTEND_URL=http://localhost:4200
```

### Rate Limiting Config:
```yaml
app:
  security:
    rate-limit:
      requests-per-minute: 100    # Global app limit
    max-transactions-per-ip-per-hour: 10  # Payment limit
```

---

## 🚀 Advantages of Simplified Architecture

### Before (Complex)
```
12 Tables
4 Payment services (MoMo, PayPal, PayOS, Generic)
Invoice generation
Payment tracking
Shipping management
Tax calculation
```

### After (Simple)
```
7 Core Tables
1 Payment service (PayOS only)
No invoices (transaction is enough)
No shipping (digital product)
No tax (included in price)
```

### Benefits:
✅ **Dễ hiểu** - Đơn giản hơn nhiều  
✅ **Dễ maintain** - Ít code hơn  
✅ **Performance tốt** - Ít join queries  
✅ **Security tập trung** - Global rate limiting  
✅ **Phù hợp business** - Bán tài khoản mail (digital product)  

---

## 📊 Data Flow Example

### Nạp 100K VND:
```
1. POST /api/wallet/deposit { amount: 100000 }
   → Rate limit check (100 req/min OK?)
   → Anti-DDoS check (pending < 3? IP < 10/hour?)
   → Anti-cheat check (duplicate?)
   
2. Create Transaction
   INSERT INTO TRANSACTIONS (
     transaction_code: 'TXN1234567890',
     type: 'DEPOSIT',
     amount: 100000,
     status: 'PENDING',
     ip_address: '123.45.67.89'
   )
   
3. Call PayOS API
   → Get QR code
   → Return to frontend
   
4. User scans QR → Pays
   
5. PayOS Webhook
   POST /api/wallet/payos/webhook {
     orderCode: 1234567890,
     status: 'PAID'
   }
   
6. Update Database (WITH LOCK)
   BEGIN TRANSACTION
   SELECT * FROM WALLETS WHERE user_id = 1 FOR UPDATE;
   UPDATE WALLETS SET balance = balance + 100000;
   UPDATE TRANSACTIONS SET status = 'SUCCESS';
   COMMIT
```

### Mua tài khoản 50K:
```
1. POST /api/orders { productId: 123 }
   → Rate limit check
   → Check wallet: balance >= 50000?
   
2. Deduct from wallet (WITH LOCK)
   BEGIN TRANSACTION
   SELECT * FROM WALLETS WHERE user_id = 1 FOR UPDATE;
   UPDATE WALLETS SET balance = balance - 50000;
   
   INSERT INTO TRANSACTIONS (
     type: 'PURCHASE',
     amount: 50000,
     status: 'SUCCESS'
   )
   
   INSERT INTO ORDERS (
     order_status: 'PAID',
     final_amount: 50000
   )
   COMMIT
   
3. Deliver product instantly
   → Update ORDER status = 'COMPLETED'
   → Send email with account details
```

---

## 🎯 Frontend Requirements

### Wallet Page:
```typescript
// Display
- Current balance
- Total deposited
- Total spent

// Actions
- Deposit button → Amount input (10K-50M)
- Show QR code modal
- Show transaction history (tabs: All | Deposit | Purchase)
```

### Deposit Flow:
```typescript
1. User clicks "Nạp tiền"
2. Input amount (validate: 10K-50M, integer only)
3. Call API → Get QR code
4. Show QR modal with:
   - QR code image
   - Payment URL button
   - Transaction code
   - Amount
5. Poll status every 5 seconds OR use WebSocket
6. Success → Close modal, refresh balance
```

### Error Handling:
```typescript
429 Too Many Requests
  → Show: "Bạn đang thao tác quá nhanh. Vui lòng đợi 1 phút."
  
10009 DUPLICATE_TRANSACTION
  → Show: "Bạn có giao dịch chưa hoàn thành. Vui lòng kiểm tra lại."
  
10100 RATE_LIMIT_EXCEEDED
  → Show: "Đã đạt giới hạn giao dịch. Vui lòng thử lại sau."
```

---

## ✅ Summary

### Đã Loại Bỏ:
- ❌ Invoice entity/service/controller
- ❌ Payment entity/service/controller
- ❌ MoMo/PayPal services
- ❌ Shipping fields
- ❌ Tax calculations
- ❌ Complex order status flow

### Giữ Lại (Core):
- ✅ Wallet (số dư)
- ✅ Transaction (lịch sử nạp tiền + mua hàng)
- ✅ Order (đơn hàng đã mua)
- ✅ PayOS (nạp tiền)

### Bổ Sung:
- ✅ **Global Rate Limiting** (chống DDoS toàn app)
- ✅ Bucket4j library
- ✅ 100 requests/minute per IP
- ✅ Simplified database schema

### Result:
**Từ 12 tables → 7 tables**  
**Từ 4 payment services → 1 service (PayOS)**  
**Chống DDoS toàn app thay vì chỉ payment endpoints**  

🎉 **Backend đã tối giản và sẵn sàng!**
