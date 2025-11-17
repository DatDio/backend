# MailShop Backend - Spring Boot 3 Application

A comprehensive, production-ready backend application built with **Spring Boot 3**, **Java 17**, **Oracle Database**, **Redis caching**, **JWT authentication**, **Google OAuth2**, and **role-based access control (RBAC)**.

## 🚀 Features

### Core Infrastructure
- **Spring Boot 3.2.0** with Java 17
- **Oracle Database** with JDBC driver (ojdbc11)
- **Redis** caching with Jedis client
- **JWT** token-based authentication (jjwt 0.12.3)
- **Google OAuth2** social login integration
- **MapStruct** for DTO-Entity mapping
- **Swagger/OpenAPI** documentation
- **Spring Boot Actuator** for monitoring
- **Thymeleaf** email templates

### Architecture
- **Layered Architecture**: Controller → Service → Repository
- **DTO Pattern** with MapStruct for clean data transfer
- **Generic API Response** wrapper (`ApiResponse<T>`)
- **Centralized Exception Handling** with custom error codes
- **Lombok** for boilerplate reduction
- **@Slf4j** logging throughout
- **@Transactional** service layer
- **@Cacheable** operations with Redis

### Security
- JWT access tokens (15 min expiry)
- JWT refresh tokens (7 days expiry)
- **API Key authentication** (per-user, similar to OpenAI/Stripe)
- Password encoding with BCrypt
- Google OAuth2 integration
- Role-Based Access Control (RBAC)
- Permission-based authorization
- @PreAuthorize annotations
- Dual authentication support (JWT + API Key)

### Modules Implemented

#### 1. User Management
- User CRUD operations
- User profile management
- Role and permission assignment
- Email verification support
- Account status management

#### 2. Role & Permission Management
- Role CRUD operations
- Permission CRUD operations
- Dynamic role-permission assignment
- Default roles: USER, ADMIN

#### 3. Authentication & Authorization
- Local registration and login
- Google OAuth2 login
- JWT token generation and validation
- Refresh token mechanism
- Session management
- Logout functionality

#### 4. Order Management
- Order creation with multiple items
- Order lifecycle (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED)
- Order cancellation support
- Order history and tracking
- Price calculations (subtotal, discount, tax, final amount)
- User-specific and admin views

#### 5. Invoice Management
- Automatic invoice generation from orders
- Invoice number generation
- Payment tracking (amount paid, balance due)
- Invoice status management (DRAFT, PENDING, PAID, OVERDUE, CANCELLED, REFUNDED)
- Scheduled overdue invoice detection
- Billing information management

#### 6. Payment Processing
- Multiple payment methods (MOMO, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY)
- Payment gateway abstraction (PaymentProviderService)
- MoMo payment integration (stub implementation)
- PayPal payment integration (stub implementation)
- Payment callback handling
- Refund processing
- Payment expiry mechanism (24 hours)
- Scheduled expired payment cleanup

#### 7. Email Service
- Asynchronous email sending
- Thymeleaf HTML templates
- Email logging and tracking
- Retry mechanism for failed emails
- Email templates:
  - Welcome email
  - Order confirmation
  - Order status updates
  - Invoice generation
  - Invoice overdue reminders
  - Payment confirmation
  - Payment failure notifications
  - Password reset
- Integration with Order, Invoice, and Payment modules

#### 8. API Key Authentication
- Per-user API key generation (similar to OpenAI/Stripe)
- Secure BCrypt hashing (never stores plaintext)
- One-time plaintext display on generation
- Multiple keys per user (max 5 active)
- Permission levels (READ_ONLY, FULL_ACCESS)
- Status management (ACTIVE, INACTIVE)
- Optional expiration timestamps
- Automatic last-used tracking
- Independent authentication from JWT
- Scheduled expired key cleanup
- RESTful key management endpoints
- See [API_KEY_FEATURE.md](API_KEY_FEATURE.md) for detailed documentation

### Data Seeding
- Automatic seeding on application startup
- 20 predefined permissions
- 2 default roles (USER, ADMIN)
- 1 admin user (configurable credentials)

## 📁 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── mymarketplace/
│   │   │           ├── AppApplication.java (Main class)
│   │   │           ├── config/
│   │   │           │   ├── RedisConfig.java
│   │   │           │   └── SecurityConfig.java
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── EmailController.java
│   │   │           │   ├── InvoiceController.java
│   │   │           │   ├── OrderController.java
│   │   │           │   ├── PaymentController.java
│   │   │           │   ├── PermissionController.java
│   │   │           │   ├── RoleController.java
│   │   │           │   └── UserController.java
│   │   │           ├── dto/
│   │   │           │   ├── ApiResponse.java (Generic wrapper)
│   │   │           │   ├── email/
│   │   │           │   │   ├── EmailRequest.java
│   │   │           │   │   └── EmailResponse.java
│   │   │           │   ├── request/ (All request DTOs)
│   │   │           │   └── response/ (All response DTOs)
│   │   │           ├── entity/
│   │   │           │   ├── BaseEntity.java (Abstract base)
│   │   │           │   ├── EmailLog.java
│   │   │           │   ├── Invoice.java
│   │   │           │   ├── Order.java
│   │   │           │   ├── OrderItem.java
│   │   │           │   ├── Payment.java
│   │   │           │   ├── Permission.java
│   │   │           │   ├── RefreshToken.java
│   │   │           │   ├── Role.java
│   │   │           │   └── User.java
│   │   │           ├── enums/
│   │   │           │   ├── AuthProvider.java
│   │   │           │   ├── InvoiceStatus.java
│   │   │           │   ├── OrderStatus.java
│   │   │           │   ├── PaymentMethod.java
│   │   │           │   └── PaymentStatus.java
│   │   │           ├── exception/
│   │   │           │   ├── BusinessException.java
│   │   │           │   ├── ErrorCode.java (Comprehensive error codes)
│   │   │           │   └── GlobalExceptionHandler.java
│   │   │           ├── mapper/ (MapStruct interfaces)
│   │   │           │   ├── EmailLogMapper.java
│   │   │           │   ├── InvoiceMapper.java
│   │   │           │   ├── OrderItemMapper.java
│   │   │           │   ├── OrderMapper.java
│   │   │           │   ├── PaymentMapper.java
│   │   │           │   ├── PermissionMapper.java
│   │   │           │   ├── RoleMapper.java
│   │   │           │   └── UserMapper.java
│   │   │           ├── repository/
│   │   │           │   ├── EmailLogRepository.java
│   │   │           │   ├── InvoiceRepository.java
│   │   │           │   ├── OrderItemRepository.java
│   │   │           │   ├── OrderRepository.java
│   │   │           │   ├── PaymentRepository.java
│   │   │           │   ├── PermissionRepository.java
│   │   │           │   ├── RefreshTokenRepository.java
│   │   │           │   ├── RoleRepository.java
│   │   │           │   └── UserRepository.java
│   │   │           ├── security/
│   │   │           │   ├── CustomOAuth2UserService.java
│   │   │           │   ├── CustomUserDetailsService.java
│   │   │           │   ├── JwtAuthenticationEntryPoint.java
│   │   │           │   ├── JwtAuthenticationFilter.java
│   │   │           │   ├── JwtTokenProvider.java
│   │   │           │   ├── OAuth2AuthenticationSuccessHandler.java
│   │   │           │   └── UserPrincipal.java
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── EmailService.java
│   │   │           │   ├── InvoiceService.java
│   │   │           │   ├── OrderService.java
│   │   │           │   ├── PaymentProviderService.java
│   │   │           │   ├── PaymentService.java
│   │   │           │   ├── PermissionService.java
│   │   │           │   ├── RoleService.java
│   │   │           │   └── UserService.java
│   │   │           ├── service/impl/
│   │   │           │   ├── AuthServiceImpl.java
│   │   │           │   ├── EmailServiceImpl.java
│   │   │           │   ├── InvoiceServiceImpl.java
│   │   │           │   ├── MoMoPaymentService.java (Stub)
│   │   │           │   ├── OrderServiceImpl.java
│   │   │           │   ├── PayPalPaymentService.java (Stub)
│   │   │           │   ├── PaymentServiceImpl.java
│   │   │           │   ├── PermissionServiceImpl.java
│   │   │           │   ├── RoleServiceImpl.java
│   │   │           │   └── UserServiceImpl.java
│   │   │           └── seeder/
│   │   │               └── DataSeeder.java
│   │   └── resources/
│   │       ├── application.yml (Main configuration)
│   │       └── templates/ (Thymeleaf email templates)
│   │           ├── invoice-overdue.html
│   │           ├── invoice.html
│   │           ├── order-confirmation.html
│   │           ├── order-status-update.html
│   │           ├── password-reset.html
│   │           ├── payment-confirmation.html
│   │           ├── payment-failed.html
│   │           └── welcome-email.html
│   └── test/
└── pom.xml
```

## 🛠️ Configuration

### Database (Oracle)
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:ORCL
    username: your_username
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
```

### Redis
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### JWT
```yaml
app:
  jwt:
    secret: your-256-bit-secret-key-here
    expiration-ms: 900000 # 15 minutes
    refresh-expiration-ms: 604800000 # 7 days
```

### Google OAuth2
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-google-client-id
            client-secret: your-google-client-secret
            scope: profile, email
```

### Email (SMTP)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Data Seeder
```yaml
app:
  seeder:
    admin:
      email: admin@mailshop.com
      password: Admin@123
```

## 🚦 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Oracle Database 19c+ (or compatible)
- Redis 6.0+

### Installation

1. **Clone the repository** (if applicable)
```bash
git clone <repository-url>
cd backend
```

2. **Configure application.yml**
   - Update database credentials
   - Set JWT secret key
   - Configure Google OAuth2 credentials
   - Configure SMTP email settings
   - Set admin user credentials

3. **Build the project**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Database Setup

The application uses JPA/Hibernate with `ddl-auto: update` mode, which will automatically create/update database tables on startup.

Oracle naming convention: All table and column names will be in **UPPER_CASE**.

## 📚 API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login with credentials
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout user

### User Management (Requires Authentication)
- `GET /api/users` - Get all users (Admin)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (Admin)
- `PUT /api/users/{userId}/roles/{roleId}` - Assign role to user (Admin)

### Order Management
- `POST /api/orders` - Create order (User)
- `GET /api/orders/my-orders` - Get user's orders
- `GET /api/orders/{id}` - Get order by ID
- `PUT /api/orders/{id}/confirm` - Confirm order (Admin)
- `PUT /api/orders/{id}/ship` - Ship order (Admin)
- `PUT /api/orders/{id}/deliver` - Deliver order (Admin)
- `PUT /api/orders/{id}/cancel` - Cancel order

### Invoice Management
- `POST /api/invoices` - Create invoice (Admin)
- `GET /api/invoices/{id}` - Get invoice by ID
- `GET /api/invoices/my-invoices` - Get user's invoices
- `PUT /api/invoices/{id}/mark-paid` - Mark invoice as paid (Admin)

### Payment Management
- `POST /api/payments` - Create payment (User)
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments/number/{paymentNumber}` - Get payment by number
- `POST /api/payments/{id}/refund` - Refund payment (Admin)
- `POST /api/payments/momo/callback` - MoMo payment callback (Public)
- `POST /api/payments/paypal/callback` - PayPal payment callback (Public)

### Email Management
- `POST /api/emails/send` - Send email (Admin)
- `GET /api/emails` - Get all email logs (Admin)
- `GET /api/emails/{id}` - Get email log by ID (Admin)
- `GET /api/emails/status/{status}` - Get emails by status (Admin)
- `POST /api/emails/retry-failed` - Retry failed emails (Admin)

### API Key Management
- `POST /api/user/apikey/generate` - Generate new API key
- `POST /api/user/apikey/revoke/{id}` - Revoke (deactivate) API key
- `POST /api/user/apikey/activate/{id}` - Activate API key
- `GET /api/user/apikey/list` - List all user's API keys
- `GET /api/user/apikey/{id}` - Get API key by ID
- `GET /api/user/apikey/usage-stats/{id}` - Get API key usage statistics

**Using API Keys:**
Include the API key in the request header:
```
X-API-KEY: msk_<your_api_key_here>
```

Example:
```bash
curl -H "X-API-KEY: msk_3xK9pL2mN8qR5tV7wY1zB4cD6fG8hJ0k" \
     http://localhost:8080/api/orders/my-orders
```

## 🔒 Security

### Default Users
After running the application, the following users will be seeded:

**Admin User:**
- Email: `admin@mailshop.com` (configurable in application.yml)
- Password: `Admin@123` (configurable in application.yml)
- Roles: ADMIN (with all permissions)

**Default Roles:**
- **USER**: Limited permissions (user:read, order:*, etc.)
- **ADMIN**: All permissions

### Permissions
The system includes 20 predefined permissions:
- `user:read`, `user:write`, `user:delete`
- `role:read`, `role:write`, `role:delete`
- `permission:read`, `permission:write`, `permission:delete`
- `order:read`, `order:write`, `order:delete`
- `invoice:read`, `invoice:write`, `invoice:delete`
- `payment:read`, `payment:write`, `payment:delete`
- `email:read`, `email:write`

## 📧 Email Templates

The application includes professionally designed HTML email templates:

1. **Welcome Email** - Sent on user registration
2. **Order Confirmation** - Sent when order is created
3. **Order Status Update** - Sent when order status changes
4. **Invoice** - Sent when invoice is generated
5. **Invoice Overdue** - Sent for overdue invoices
6. **Payment Confirmation** - Sent on successful payment
7. **Payment Failed** - Sent on failed payment
8. **Password Reset** - Sent for password reset requests

All templates are responsive and use gradient backgrounds with professional styling.

## ⚡ Payment Integration

The application includes stub implementations for payment gateways:

### MoMo Payment (Stub)
- Mock payment URL generation
- Callback verification (accepts all for demo)
- Transaction ID extraction
- Refund processing simulation

### PayPal Payment (Stub)
- Mock sandbox/live URL generation
- Callback verification (checks paymentId and PayerID)
- Transaction ID extraction
- Refund processing simulation

**Note:** These are stub implementations for demonstration purposes. Replace with actual API integration for production use.

## 📊 Monitoring

Spring Boot Actuator endpoints are available at:
```
http://localhost:8080/actuator
```

Available endpoints:
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties

## 🔄 Scheduled Tasks

The application runs the following scheduled tasks:

1. **Overdue Invoice Check** - Runs daily at midnight
   - Checks for overdue invoices
   - Updates invoice status to OVERDUE
   - Sends reminder emails

2. **Expired Payment Cleanup** - Runs every hour
   - Finds payments pending for more than 24 hours
   - Updates payment status to EXPIRED

3. **Failed Email Retry** - Runs every hour
   - Retries failed emails
   - Limited to 3 retry attempts per email

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📝 Best Practices Implemented

- ✅ Layered architecture (Controller → Service → Repository)
- ✅ DTO pattern with MapStruct
- ✅ Generic API response wrapper
- ✅ Centralized exception handling
- ✅ Comprehensive error codes
- ✅ Logging with @Slf4j
- ✅ Transaction management with @Transactional
- ✅ Caching with Redis
- ✅ Async email sending
- ✅ JWT token-based authentication
- ✅ Role-based access control
- ✅ Input validation
- ✅ API documentation with Swagger
- ✅ Scheduled task execution
- ✅ Payment gateway abstraction
- ✅ Email template management

## 🔧 Troubleshooting

### Database Connection Issues
- Verify Oracle database is running
- Check connection URL, username, and password in application.yml
- Ensure Oracle JDBC driver is compatible with your database version

### Redis Connection Issues
- Verify Redis server is running: `redis-cli ping` (should return PONG)
- Check Redis host and port in application.yml

### Email Sending Issues
- Verify SMTP credentials
- Enable "Less secure app access" or use App Passwords for Gmail
- Check firewall settings for SMTP port

### JWT Token Issues
- Ensure JWT secret key is at least 256 bits (32 characters)
- Check token expiration times

## 📄 License

This project is created for demonstration purposes.

## 👥 Contact

For questions or support, please contact the development team.

---

**Built with ❤️ using Spring Boot 3, Java 17, and modern backend technologies**
