# API Key Authentication System

A comprehensive per-user API key authentication system similar to OpenAI and Stripe, integrated with the existing Spring Boot backend.

## 📋 Overview

This module adds API key-based authentication as an alternative to JWT tokens. Users can generate, manage, and use API keys to authenticate requests without needing to handle JWT tokens.

### Key Features

- ✅ **Per-user API keys** - Each user can have multiple API keys
- ✅ **Secure hashing** - API keys are stored using BCrypt (never plaintext)
- ✅ **One-time display** - Plaintext key shown only once during generation
- ✅ **Active key limit** - Maximum 5 active keys per user
- ✅ **Permission levels** - READ_ONLY or FULL_ACCESS
- ✅ **Status management** - ACTIVE or INACTIVE
- ✅ **Expiration support** - Optional expiry timestamps
- ✅ **Usage tracking** - Automatic LAST_USED_AT updates
- ✅ **Independent authentication** - Works alongside JWT (JWT has priority)
- ✅ **Scheduled cleanup** - Auto-deactivate expired keys hourly

## 🏗️ Architecture

### Database Schema

**Table: API_KEYS**
```sql
ID              RAW(16)         PRIMARY KEY
USER_ID         NUMBER(19)      FOREIGN KEY → USERS.ID
KEY_HASH        VARCHAR2(255)   BCrypt hashed value
PERMISSION      VARCHAR2(50)    READ_ONLY | FULL_ACCESS
STATUS          VARCHAR2(50)    ACTIVE | INACTIVE
CREATED_AT      TIMESTAMP       Auto-generated
UPDATED_AT      TIMESTAMP       Auto-updated
EXPIRED_AT      TIMESTAMP       Optional
LAST_USED_AT    TIMESTAMP       Updated on each use
NAME            VARCHAR2(100)   Optional friendly name
DESCRIPTION     VARCHAR2(500)   Optional description
```

### API Key Format

```
msk_<base64_encoded_random_bytes>
```

Example: `msk_3xK9pL2mN8qR5tV7wY1zB4cD6fG8hJ0k`

- **Prefix**: `msk` (MailShop Key)
- **Length**: 32 bytes (256 bits) of secure random data
- **Encoding**: Base64 URL-safe without padding

## 🔧 Components

### 1. Entity Layer
**Location**: `entity/ApiKey.java`

```java
@Entity
@Table(name = "API_KEYS")
public class ApiKey extends BaseEntity {
    private UUID id;
    private User user;
    private String keyHash;
    private ApiKeyPermission permission;
    private ApiKeyStatus status;
    private LocalDateTime expiredAt;
    private LocalDateTime lastUsedAt;
    // ... helper methods
}
```

### 2. Enums
**Location**: `enums/`

- `ApiKeyStatus.java` - ACTIVE, INACTIVE
- `ApiKeyPermission.java` - READ_ONLY, FULL_ACCESS

### 3. Repository Layer
**Location**: `repository/ApiKeyRepository.java`

Key queries:
- Find by user
- Find active keys
- Count active keys per user
- Find expired active keys (for scheduled cleanup)

### 4. Service Layer
**Location**: `service/ApiKeyService.java` & `service/impl/ApiKeyServiceImpl.java`

Operations:
- Generate new API key
- Revoke (deactivate) key
- Activate key
- List user's keys
- Validate API key
- Update last used timestamp
- Scheduled expiry cleanup

### 5. Controller Layer
**Location**: `controller/ApiKeyController.java`

Base path: `/api/user/apikey`

Endpoints:
- `POST /generate` - Generate new API key
- `POST /revoke/{id}` - Revoke an API key
- `POST /activate/{id}` - Activate an API key
- `GET /list` - List all user's API keys
- `GET /usage-stats/{id}` - Get usage statistics (placeholder)
- `GET /{id}` - Get API key details by ID

### 6. Security Layer
**Location**: `security/ApiKeyAuthenticationFilter.java`

Authentication flow:
1. Check if request already authenticated (JWT priority)
2. Extract `X-API-KEY` header
3. Validate key format
4. Match hashed key in database
5. Check expiration
6. Load user details
7. Set SecurityContext
8. Track last used timestamp

### 7. Utility Layer
**Location**: `util/ApiKeyGenerator.java`

Responsibilities:
- Generate secure random API keys
- Validate API key format
- Provide prefix constant

## 🚀 Usage Examples

### 1. Generate API Key

**Request:**
```http
POST /api/user/apikey/generate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "Production API Key",
  "description": "Used for production server integration",
  "permission": "FULL_ACCESS",
  "expiredAt": "2025-12-31T23:59:59"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "keyMetadata": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Production API Key",
      "description": "Used for production server integration",
      "permission": "FULL_ACCESS",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00",
      "expiredAt": "2025-12-31T23:59:59",
      "lastUsedAt": null,
      "expired": false,
      "valid": true
    },
    "apiKey": "msk_3xK9pL2mN8qR5tV7wY1zB4cD6fG8hJ0k",
    "warning": "This is the only time you will see this API key. Please store it securely."
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

⚠️ **Important**: The plaintext `apiKey` is shown **only once**. Store it securely!

### 2. Use API Key for Authentication

**Request:**
```http
GET /api/orders/my-orders
X-API-KEY: msk_3xK9pL2mN8qR5tV7wY1zB4cD6fG8hJ0k
```

**Response:**
```json
{
  "success": true,
  "data": [...],
  "timestamp": "2024-01-15T10:35:00"
}
```

### 3. List API Keys

**Request:**
```http
GET /api/user/apikey/list
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Production API Key",
      "permission": "FULL_ACCESS",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00",
      "lastUsedAt": "2024-01-15T10:35:00",
      "expired": false,
      "valid": true
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "Testing API Key",
      "permission": "READ_ONLY",
      "status": "INACTIVE",
      "createdAt": "2024-01-10T08:00:00",
      "lastUsedAt": "2024-01-12T15:30:00",
      "expired": false,
      "valid": false
    }
  ],
  "timestamp": "2024-01-15T10:40:00"
}
```

### 4. Revoke API Key

**Request:**
```http
POST /api/user/apikey/revoke/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "INACTIVE",
    "valid": false
  },
  "timestamp": "2024-01-15T11:00:00"
}
```

## 🔒 Security Features

### 1. Secure Key Generation
- Uses `SecureRandom` for cryptographically strong random bytes
- 256-bit (32 bytes) key length
- Base64 URL-safe encoding

### 2. Hash Storage
- API keys stored using BCrypt hashing
- Same security level as passwords
- Never stores plaintext keys in database

### 3. Authentication Priority
```
1. JWT Token (if present) - highest priority
2. API Key (X-API-KEY header) - fallback
3. Unauthenticated - lowest priority
```

If both JWT and API key are provided, JWT is used.

### 4. Validation Checks
- ✅ Format validation (prefix + base64)
- ✅ Hash matching
- ✅ Status check (ACTIVE only)
- ✅ Expiration check
- ✅ User association

### 5. Rate Limiting (Future Enhancement)
- Track API key usage
- Implement per-key rate limits
- Alert on suspicious activity

## ⚙️ Configuration

### Application Properties

No additional configuration required. The system uses existing:
- `PasswordEncoder` bean (BCrypt)
- `SecurityFilterChain` configuration
- Database connection settings

### Scheduled Tasks

**Expired Key Cleanup:**
```java
@Scheduled(cron = "0 0 * * * *") // Every hour
public void deactivateExpiredKeys()
```

Automatically deactivates API keys where:
- `EXPIRED_AT IS NOT NULL`
- `EXPIRED_AT < CURRENT_TIMESTAMP`
- `STATUS = 'ACTIVE'`

## 📊 Error Codes

New error codes added to `ErrorCode.java`:

| Code | Message | Description |
|------|---------|-------------|
| 9000 | API key not found | API key with given ID doesn't exist |
| 9001 | Invalid API key | API key format invalid or doesn't match any hash |
| 9002 | API key has expired | API key's expiration date has passed |
| 9003 | Maximum API keys limit reached | User has reached max active keys (5) |
| 9004 | API key is already active | Attempted to activate already active key |
| 9005 | API key is already inactive | Attempted to deactivate already inactive key |

## 🧪 Testing

### Manual Testing with cURL

**1. Generate API Key:**
```bash
curl -X POST http://localhost:8080/api/user/apikey/generate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Key",
    "permission": "FULL_ACCESS"
  }'
```

**2. Use API Key:**
```bash
curl -X GET http://localhost:8080/api/orders/my-orders \
  -H "X-API-KEY: msk_YOUR_API_KEY_HERE"
```

**3. List Keys:**
```bash
curl -X GET http://localhost:8080/api/user/apikey/list \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Postman Collection

Import these endpoints into Postman:

```json
{
  "info": {
    "name": "API Key Authentication",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Generate API Key",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/user/apikey/generate",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Test Key\",\n  \"permission\": \"FULL_ACCESS\"\n}"
        }
      }
    }
  ]
}
```

## 🔄 Integration with Existing Modules

### JWT Authentication
- API key filter runs **before** JWT filter
- JWT has priority if both are present
- Seamless fallback to API key if no JWT

### User Module
- API keys are tied to User entities
- Uses existing `UserRepository`
- Inherits user's roles and permissions

### Authorization
- API keys respect existing `@PreAuthorize` annotations
- Permission levels (READ_ONLY/FULL_ACCESS) can be extended
- Works with RBAC system

### Caching
- Uses existing Redis cache configuration
- Cache key: `apikeys`
- Cache eviction on key updates

## 📈 Future Enhancements

1. **Rate Limiting**
   - Per-key request limits
   - Time-window based throttling
   - Alert on threshold exceeded

2. **Usage Analytics**
   - Request count tracking
   - Endpoint usage statistics
   - Geographic usage data

3. **IP Whitelisting**
   - Restrict API key usage by IP
   - Multiple IP support
   - CIDR notation support

4. **Scopes/Permissions**
   - Fine-grained permissions beyond READ_ONLY/FULL_ACCESS
   - Resource-level access control
   - Custom scope definitions

5. **Audit Logging**
   - Log all API key operations
   - Track authentication attempts
   - Security event monitoring

6. **Webhook Support**
   - Notify on suspicious activity
   - Alert on key expiration
   - Usage threshold notifications

## 🐛 Troubleshooting

### API Key Not Working

**Check:**
1. ✅ Key format correct? `msk_<base64>`
2. ✅ Key status is ACTIVE?
3. ✅ Key not expired?
4. ✅ Header name correct: `X-API-KEY`
5. ✅ User account active?

**Debug:**
```bash
# Check logs for authentication errors
tail -f logs/application.log | grep "API key"

# Verify key in database (hash only, not plaintext)
SELECT ID, USER_ID, STATUS, EXPIRED_AT, LAST_USED_AT 
FROM API_KEYS 
WHERE USER_ID = ?;
```

### Cannot Generate More Keys

**Error**: `Maximum API keys limit reached (5)`

**Solution:**
- Revoke unused keys: `POST /api/user/apikey/revoke/{id}`
- Delete old keys from database (admin)
- Increase `MAX_ACTIVE_KEYS_PER_USER` constant

### Filter Not Running

**Check:**
1. ✅ `ApiKeyAuthenticationFilter` bean created?
2. ✅ Filter added to security chain?
3. ✅ Filter order correct (before JWT filter)?

**Fix:**
```java
// In SecurityConfig.java
.addFilterBefore(apiKeyAuthenticationFilter, JwtAuthenticationFilter.class)
```

## 📝 Best Practices

### For Users

1. **Store Securely**: Save API key immediately after generation
2. **Use Environment Variables**: Never hardcode keys in source code
3. **Rotate Regularly**: Generate new keys, revoke old ones
4. **Set Expiration**: Use `expiredAt` for time-limited keys
5. **Name Meaningfully**: Use descriptive names for easy identification

### For Developers

1. **Never Log Keys**: Mask API keys in logs
2. **Validate Format**: Use `ApiKeyGenerator.isValidFormat()`
3. **Handle Exceptions**: Catch `BusinessException` for API key errors
4. **Update Last Used**: Tracked automatically by filter
5. **Test Both Auth Methods**: Ensure JWT and API key work independently

## 📚 References

- [OpenAI API Keys](https://platform.openai.com/docs/api-reference/authentication)
- [Stripe API Keys](https://stripe.com/docs/keys)
- [Spring Security Filter Chain](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [BCrypt Password Hashing](https://en.wikipedia.org/wiki/Bcrypt)

---

**Built with security and scalability in mind** 🔐
