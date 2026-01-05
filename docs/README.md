# Retype Documentation - Deployment Guide

## Cấu trúc thư mục

```
docs/
├── retype.yml              # Config file
├── index.md                # Trang chủ
├── kiem-tra-so-du.md       # API: Kiểm tra số dư
├── lay-danh-sach-san-pham.md  # API: Lấy danh sách sản phẩm
└── mua-mail.md             # API: Mua mail
```

---

## Cài đặt Retype

### Trên máy local (Windows)
```bash
# Cài đặt Retype CLI
dotnet tool install retypeapp --global

# Hoặc dùng npm
npm install retypeapp --global
```

### Trên VPS (Ubuntu)
```bash
# Cài dotnet runtime
sudo apt update
sudo apt install -y dotnet-runtime-6.0

# Cài Retype
dotnet tool install retypeapp --global
export PATH="$PATH:$HOME/.dotnet/tools"
```

---

## Build & Preview

```bash
# Di chuyển đến thư mục docs
cd docs

# Preview local (port 5000)
retype start

# Build static files
retype build
```

---

## Deploy lên VPS

### Option 1: Serve static files với Nginx

```bash
# Build
cd /opt/mailshop/docs
retype build

# Copy output sang nginx
sudo cp -r .retype/* /var/www/docs.emailsieure.com/
```

Nginx config (`/etc/nginx/sites-available/docs`):
```nginx
server {
    listen 80;
    server_name docs.emailsieure.com;
    root /var/www/docs.emailsieure.com;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### Option 2: GitHub Pages (miễn phí)

1. Push thư mục `docs/` lên GitHub
2. Vào Settings > Pages
3. Chọn source: Deploy from a branch
4. Retype sẽ tự build qua GitHub Actions

---

## Custom domain

1. Thêm CNAME record: `docs.emailsieure.com` → VPS IP
2. Cập nhật `retype.yml`:
   ```yaml
   url: https://docs.emailsieure.com
   ```
