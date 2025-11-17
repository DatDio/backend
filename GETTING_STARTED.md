# 🚀 Hướng Dẫn Chạy Project MailShop DragonVu

## 📋 Yêu Cầu Hệ Thống

- **Java 17** (JDK 17 trở lên)
- **Maven 3.6+** (hoặc dùng Maven wrapper có sẵn)
- **Docker Desktop** (để chạy Oracle DB + Redis)
- **Git** (để clone/pull code)

---

## 🗄️ BƯỚC 1: Khởi Động Database & Redis

### Option 1: Chạy bằng Docker (Khuyến nghị ⭐)

```bash
# Bước 1.1: Login Oracle Container Registry (chỉ lần đầu tiên)
docker login container-registry.oracle.com
# Username: your-oracle-account@email.com
# Password: your-oracle-password
# Đăng ký tại: https://profile.oracle.com/myprofile/account/create-account.jspx
# Accept terms: https://container-registry.oracle.com/

# Bước 1.2: Tạo file .env từ template
cp .env.example .env

# Bước 1.3: Chỉnh sửa .env với thông tin thật của bạn
notepad .env

# Bước 1.4: Khởi động tất cả services (Oracle + Redis + Backend)
docker-compose up -d

# Bước 1.5: Xem logs
docker-compose logs -f
```

**Chờ 2-3 phút để Oracle DB khởi tạo lần đầu!**

### Option 2: Cài Oracle XE Trực Tiếp (Không dùng Docker)

#### Windows:

1. Download Oracle XE 21c:
   - Link: https://www.oracle.com/database/technologies/xe-downloads.html
   
2. Cài đặt Oracle XE 21c
   - System password: `OraclePassword123`
   - Port: `1521`
   - SID: `XE`

3. Tạo user cho MailShop:

```sql
-- Mở SQL*Plus với system user
sqlplus system/OraclePassword123@localhost:1521/XE

-- Chạy các lệnh sau:
CREATE USER mailshop_user IDENTIFIED BY mailshop_pass;
GRANT CONNECT, RESOURCE TO mailshop_user;
GRANT CREATE SESSION TO mailshop_user;
GRANT CREATE TABLE TO mailshop_user;
GRANT UNLIMITED TABLESPACE TO mailshop_user;
ALTER USER mailshop_user DEFAULT TABLESPACE USERS;
COMMIT;
EXIT;
```

4. Cài Redis trên Windows:

```bash
# Dùng Chocolatey
choco install redis-64

# Hoặc download từ: https://github.com/microsoftarchive/redis/releases
# Sau đó start Redis:
redis-server
```

---

## ⚙️ BƯỚC 2: Cấu Hình Application

### Option A: Dùng File `.env` (Khuyến nghị)

Tạo file `.env` trong thư mục `backend/`:

```env
# Database
DB_USERNAME=mailshop_user
DB_PASSWORD=mailshop_pass

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Secret (thay đổi trong production!)
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-256-bits

# PayOS Configuration (Đăng ký tại: https://payos.vn/)
PAYOS_CLIENT_ID=your-payos-client-id
PAYOS_API_KEY=your-payos-api-key
PAYOS_CHECKSUM_KEY=your-payos-checksum-key

# Email Configuration (Gmail App Password)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# Google OAuth2 (Optional - Đăng ký tại: https://console.cloud.google.com/)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Admin Default Credentials
ADMIN_EMAIL=admin@mailshop.vn
ADMIN_PASSWORD=Admin@123456

# Frontend URL
FRONTEND_URL=http://localhost:4200
```

### Option B: Chỉnh Sửa Trực Tiếp `application.yml`

Mở file `src/main/resources/application.yml` và thay đổi:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: mailshop_user
    password: mailshop_pass
    
  data:
    redis:
      host: localhost
      port: 6379
      password: # Để trống nếu Redis không có password
```

---

## 🏃 BƯỚC 3: Chạy Backend

### Option 1: Chạy với Maven (Development)

```bash
# Từ thư mục backend/
cd "d:\Job Freelance\mailshop_dragonvu\backend"

# Build project (bỏ qua test)
mvn clean package -DskipTests

# Hoặc chạy trực tiếp (hot reload)
mvn spring-boot:run
```

### Option 2: Chạy với Java (Production Build)

```bash
# Build JAR file
mvn clean package -DskipTests

# Chạy JAR
java -jar target/mailshop-backend-0.0.1-SNAPSHOT.jar
```

### Option 3: Chạy bằng IDE (IntelliJ IDEA / Eclipse)

1. **Import project**: File → Open → Chọn thư mục `backend`
2. **Wait for Maven** indexing hoàn tất
3. **Tìm main class**: `MailshopBackendApplication.java`
4. **Right-click** → Run 'MailshopBackendApplication'

---

## ✅ BƯỚC 4: Kiểm Tra Hoạt Động

### 1. Kiểm tra Health Check:

```bash
curl http://localhost:8080/api/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

### 2. Kiểm tra Database Connection:

- Mở browser: http://localhost:8080/api/actuator/health
- Xem logs: Nếu thấy `HHH000400: Using dialect: org.hibernate.dialect.OracleDialect` → ✅ OK

### 3. Kiểm tra API Swagger UI:

- URL: http://localhost:8080/api/swagger-ui/index.html
- Bạn sẽ thấy tất cả API endpoints

### 4. Test API đầu tiên - Register:

```bash
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"test@gmail.com","password":"Test@123456","fullName":"Test User"}'
```

---

## 🗄️ Kết Nối Database Bằng Tools

### SQL Developer / DBeaver / DataGrip:

```
Connection Name: MailShop Oracle
Connection Type: Oracle
Host: localhost
Port: 1521
SID: XE
Username: mailshop_user
Password: mailshop_pass
```

### Kiểm tra tables đã tạo:

```sql
-- Kết nối với mailshop_user
SELECT table_name FROM user_tables ORDER BY table_name;
```

**Bạn sẽ thấy các bảng:**
- USERS
- ROLES
- USER_ROLES
- WALLETS
- TRANSACTIONS
- ORDERS
- ORDER_ITEMS
- API_KEYS
- EMAIL_LOGS

---

## 🔐 Default Admin Account

Sau khi chạy lần đầu, hệ thống tự động tạo admin account:

```
Email: admin@mailshop.vn
Password: Admin@123456
```

**Test login:**

```bash
# PowerShell
$body = @{
    email = "admin@mailshop.vn"
    password = "Admin@123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

**Response sẽ có:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1Ni...",
  "refreshToken": "eyJhbGciOiJIUzI1Ni...",
  "tokenType": "Bearer"
}
```

---

## 🐛 Xử Lý Lỗi Thường Gặp

### 1. Lỗi: "Cannot connect to database"

**Nguyên nhân:**
- Oracle DB chưa chạy
- Sai username/password
- Port 1521 bị chặn

**Giải quyết:**

```bash
# Kiểm tra Oracle đang chạy (Docker)
docker ps | findstr oracle

# Hoặc test kết nối
sqlplus mailshop_user/mailshop_pass@localhost:1521/XE
```

### 2. Lỗi: "Unable to connect to Redis"

**Giải quyết:**

```bash
# Kiểm tra Redis (Docker)
docker ps | findstr redis

# Hoặc test Redis
redis-cli -h localhost -p 6379 ping
# Expected: PONG
```

### 3. Lỗi: "Port 8080 already in use"

**Giải quyết:**

```bash
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process (thay <PID>)
taskkill /PID <PID> /F

# Hoặc đổi port trong application.yml
server:
  port: 8081
```

### 4. Lỗi: "Table or view does not exist"

**Nguyên nhân:** JPA chưa tạo tables

**Giải quyết:**

Kiểm tra `application.yml`:

```yaml
jpa:
  hibernate:
    ddl-auto: update  # Phải là 'update', không phải 'none' hoặc 'validate'
```

Xóa logs và restart:

```bash
mvn clean spring-boot:run
```

### 5. Lỗi Build: "Error compiling..."

**Giải quyết:**

```bash
# Clean và rebuild
mvn clean install -DskipTests

# Nếu vẫn lỗi, kiểm tra Java version
java -version
# Phải là Java 17

# Kiểm tra Maven
mvn -version
```

---

## 📊 Monitoring & Logs

### Xem Logs:

```bash
# Logs được lưu tại:
tail -f logs/application.log

# Hoặc xem logs Docker
docker-compose logs -f backend
```

### Actuator Endpoints:

- Health: http://localhost:8080/api/actuator/health
- Metrics: http://localhost:8080/api/actuator/metrics
- Info: http://localhost:8080/api/actuator/info

---

## 🔄 Workflow Phát Triển

### 1. Làm việc hàng ngày:

```bash
# Start database
docker-compose up -d oracle-db redis

# Run backend với hot reload
mvn spring-boot:run

# Hoặc dùng IDE Run
```

### 2. Khi có thay đổi code:

- **Hot reload tự động** nếu dùng `mvn spring-boot:run`
- Hoặc **Restart** IDE run configuration

### 3. Khi thay đổi entity:

JPA với `ddl-auto: update` sẽ **tự động update schema**, không cần chạy migration thủ công!

### 4. Test API:

- **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
- **Postman**: Import OpenAPI JSON từ Swagger
- **cURL/PowerShell**: Test từ terminal

---

## 🚀 Deploy Production

### Build Docker Image:

```bash
# Build image
docker build -t mailshop-backend:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DB_USERNAME=mailshop_user \
  -e DB_PASSWORD=mailshop_pass \
  --name mailshop-backend \
  mailshop-backend:latest
```

### Hoặc dùng Docker Compose:

```bash
docker-compose up -d
```

---

## 📞 Quick Commands Cheat Sheet

```bash
# ==== Docker ====
docker-compose up -d              # Start tất cả
docker-compose down              # Stop tất cả
docker-compose logs -f backend   # Xem logs backend
docker-compose restart backend   # Restart backend

# ==== Maven ====
mvn clean package -DskipTests    # Build JAR
mvn spring-boot:run             # Run với hot reload
mvn clean                       # Clean build artifacts

# ==== Database ====
docker exec -it mailshop-oracle sqlplus mailshop_user/mailshop_pass@XE
# Hoặc
sqlplus mailshop_user/mailshop_pass@localhost:1521/XE

# ==== Redis ====
docker exec -it mailshop-redis redis-cli
# Hoặc
redis-cli -h localhost -p 6379

# ==== Test APIs ====
curl http://localhost:8080/api/actuator/health
curl http://localhost:8080/api/swagger-ui/index.html
```

---

## 📚 Tài Liệu Thêm

- **API Documentation**: http://localhost:8080/api/swagger-ui/index.html
- **Architecture Guide**: `SIMPLIFIED_ARCHITECTURE.md`
- **Docker Guide**: `DOCKER_GUIDE.md`
- **Code Structure**: Xem `README.md` (nếu có)

---

## ✅ Checklist Lần Đầu Chạy

- [ ] Cài Java 17
- [ ] Cài Docker Desktop
- [ ] Login Oracle Container Registry
- [ ] Tạo file `.env` với thông tin thật
- [ ] Chạy `docker-compose up -d`
- [ ] Đợi 2-3 phút Oracle khởi tạo
- [ ] Chạy `mvn spring-boot:run`
- [ ] Test health check: `curl http://localhost:8080/api/actuator/health`
- [ ] Mở Swagger UI: http://localhost:8080/api/swagger-ui/index.html
- [ ] Login với admin: `admin@mailshop.vn` / `Admin@123456`

---

🎉 **Xong! Backend đã sẵn sàng!**

Nếu gặp lỗi, tham khảo phần "🐛 Xử Lý Lỗi Thường Gặp" ở trên.
