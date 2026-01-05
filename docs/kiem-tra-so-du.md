---
label: Kiểm tra số dư
icon: verified
order: 90
---

# 💰 Kiểm tra số dư

Lấy thông tin số dư và thứ hạng của tài khoản.

---

## API Endpoint

+++ GET
```
https://emailsieure.com/api/v1/users/balance?apikey=YOUR_API_KEY
```
+++

---

## Tham số truy vấn

| Tham số | Kiểu | Required | Mô tả |
|---------|------|----------|-------|
| `apikey` | string | :icon-check-circle: Required | API Key của bạn, lấy tại [đây](https://emailsieure.com/settings) |

---

## Response

```json
{
    "success": true,
    "data": {
        "email": "user@emailsieure.com",
        "balance": 203141999,
        "totalDeposit": 204164999,
        "totalSpent": 3450,
        "rankName": "Bạc",
        "bonusPercent": 5
    },
    "timestamp": "2026-01-05T09:40:22.0064432"
}
```

---

## Giải thích Response

| Field | Kiểu | Mô tả |
|-------|------|-------|
| `email` | string | Email đăng nhập |
| `balance` | number | Số dư hiện tại (VNĐ) |
| `totalDeposit` | number | Tổng tiền đã nạp |
| `totalSpent` | number | Tổng tiền đã chi tiêu |
| `rankName` | string | Thứ hạng hiện tại |
| `bonusPercent` | number | % bonus khi nạp tiền |
