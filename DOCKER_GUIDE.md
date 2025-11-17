# MailShop DragonVu - Docker Deployment Guide

## 📋 Prerequisites

1. **Docker Desktop** installed
   - Download: https://www.docker.com/products/docker-desktop
   - Minimum: 8GB RAM allocated to Docker

2. **Oracle Database Docker Image**
   - Login to Oracle Container Registry:
   ```bash
   docker login container-registry.oracle.com
   Username: your-oracle-account@email.com
   Password: your-oracle-password
   ```
   - Accept terms: https://container-registry.oracle.com/

## 🚀 Quick Start

### 1. Setup Environment Variables

```bash
# Copy example file
cp .env.example .env

# Edit .env file with your actual credentials
notepad .env  # Windows
nano .env     # Linux/Mac
```

**Required variables:**
```env
PAYOS_CLIENT_ID=your-actual-client-id
PAYOS_API_KEY=your-actual-api-key
PAYOS_CHECKSUM_KEY=your-actual-checksum-key
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

### 2. Start All Services

```bash
# Start all containers (Oracle DB + Redis + Backend)
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
docker-compose logs -f oracle-db
docker-compose logs -f redis
```

### 3. Wait for Initialization

**First time startup:**
- Oracle DB: ~2-3 minutes to initialize
- Backend: ~1-2 minutes after DB is ready
- Total: ~3-5 minutes

**Check status:**
```bash
# Check all containers
docker-compose ps

# Check health status
docker-compose ps | grep healthy
```

### 4. Access Application

- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
- **Health Check**: http://localhost:8080/api/actuator/health
- **Oracle EM Express**: https://localhost:5500/em

## 📊 Database Access

### Connect to Oracle Database

**Via Docker:**
```bash
# Access SQL*Plus inside container
docker exec -it mailshop-oracle sqlplus mailshop_user/mailshop_pass@localhost:1521/XE

# Run SQL command
docker exec mailshop-oracle sqlplus -s mailshop_user/mailshop_pass@localhost:1521/XE <<< "SELECT COUNT(*) FROM USERS;"
```

**Via SQL Developer:**
```
Connection Name: MailShop Oracle
Username: mailshop_user
Password: mailshop_pass
Hostname: localhost
Port: 1521
SID: XE
```

**Via DBeaver:**
```
Host: localhost
Port: 1521
Database: XE
Username: mailshop_user
Password: mailshop_pass
```

### System User (DBA Access)

```bash
docker exec -it mailshop-oracle sqlplus system/OraclePassword123@localhost:1521/XE
```

## 🔧 JPA Configuration

**Current setting in application.yml:**
```yaml
jpa:
  hibernate:
    ddl-auto: update  # Auto-create/update tables
```

**Behavior:**
- First startup: JPA creates all tables automatically
- Restart: JPA updates schema if entity changes detected
- No manual SQL migration needed (Flyway disabled)

**Tables created automatically:**
```
✓ USERS
✓ ROLES
✓ PERMISSIONS
✓ USER_ROLES
✓ ROLE_PERMISSIONS
✓ WALLETS
✓ TRANSACTIONS
✓ ORDERS
✓ ORDER_ITEMS
✓ API_KEYS
✓ EMAIL_LOGS
```

## 🛠️ Common Commands

### Stop & Start

```bash
# Stop all containers
docker-compose stop

# Start stopped containers
docker-compose start

# Restart all containers
docker-compose restart

# Restart specific service
docker-compose restart backend
```

### Clean & Rebuild

```bash
# Stop and remove containers
docker-compose down

# Remove containers + volumes (⚠️ deletes all data)
docker-compose down -v

# Rebuild backend image
docker-compose build backend

# Rebuild and restart
docker-compose up -d --build
```

### View Logs

```bash
# All logs (follow)
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100

# Backend logs only
docker-compose logs -f backend

# Oracle logs
docker-compose logs -f oracle-db
```

### Execute Commands

```bash
# Backend container bash
docker exec -it mailshop-backend sh

# Oracle container bash
docker exec -it mailshop-oracle bash

# Redis CLI
docker exec -it mailshop-redis redis-cli -a RedisPassword123
```

## 🗄️ Data Persistence

**Volumes:**
- `oracle-data`: Oracle database files (persists across restarts)
- `redis-data`: Redis snapshots
- `./logs`: Application logs (host machine)

**Backup database:**
```bash
# Export data
docker exec mailshop-oracle expdp mailshop_user/mailshop_pass \
  directory=DATA_PUMP_DIR \
  dumpfile=mailshop_backup.dmp \
  schemas=mailshop_user
```

## 🔍 Troubleshooting

### Oracle Container Won't Start

```bash
# Check logs
docker-compose logs oracle-db

# Common issue: Not enough memory
# Solution: Allocate at least 8GB RAM to Docker Desktop

# Reset Oracle volume
docker-compose down -v
docker-compose up -d oracle-db
```

### Backend Can't Connect to Database

```bash
# Check if Oracle is healthy
docker-compose ps oracle-db

# Wait for "healthy" status (takes 2-3 minutes)

# Check connection from backend
docker exec mailshop-backend curl http://oracle-db:1521
```

### JPA Schema Update Issues

```bash
# Check Hibernate logs
docker-compose logs backend | grep "Hibernate"

# Force recreate tables (⚠️ loses data)
# Change ddl-auto to 'create-drop', restart, then change back to 'update'
```

### Redis Connection Failed

```bash
# Test Redis connection
docker exec mailshop-backend redis-cli -h redis -p 6379 -a RedisPassword123 ping

# Should return: PONG
```

## 📝 Production Deployment

### 1. Security Checklist

```bash
# Change all default passwords
- Oracle: ORACLE_PWD
- Redis: requirepass
- Admin: ADMIN_PASSWORD
- JWT: JWT_SECRET (min 256 bits)

# Use secrets management (Docker Secrets, Kubernetes Secrets)
# Don't commit .env file to git
# Enable HTTPS/SSL
# Configure firewall rules
```

### 2. Performance Tuning

```yaml
# docker-compose.yml adjustments
backend:
  environment:
    JAVA_OPTS: -Xms1g -Xmx2g  # Increase heap size
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 2G
      reservations:
        cpus: '1'
        memory: 1G
```

### 3. Monitoring

```bash
# Container stats
docker stats

# Application health
curl http://localhost:8080/api/actuator/health

# Prometheus metrics
curl http://localhost:8080/api/actuator/prometheus
```

## 🔗 Useful Links

- **Swagger API Docs**: http://localhost:8080/api/swagger-ui/index.html
- **Health Check**: http://localhost:8080/api/actuator/health
- **Metrics**: http://localhost:8080/api/actuator/metrics
- **Oracle EM**: https://localhost:5500/em (user: system, pass: OraclePassword123)

## 📞 Support

**Check service status:**
```bash
docker-compose ps
```

**All services should show "Up" and "healthy"**

**Quick test:**
```bash
# Test backend health
curl http://localhost:8080/api/actuator/health

# Expected: {"status":"UP"}
```

## ⚙️ Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| DB_USERNAME | mailshop_user | Database username |
| DB_PASSWORD | mailshop_pass | Database password |
| REDIS_PASSWORD | RedisPassword123 | Redis password |
| JWT_SECRET | (auto) | JWT signing key |
| PAYOS_CLIENT_ID | - | PayOS client ID |
| PAYOS_API_KEY | - | PayOS API key |
| PAYOS_CHECKSUM_KEY | - | PayOS checksum key |
| MAIL_USERNAME | - | SMTP email address |
| MAIL_PASSWORD | - | SMTP app password |
| FRONTEND_URL | http://localhost:4200 | Angular frontend URL |
| ADMIN_EMAIL | admin@mailshop.vn | Default admin email |
| ADMIN_PASSWORD | Admin@123456 | Default admin password |

---

🎉 **Your MailShop backend is now running in Docker!**
