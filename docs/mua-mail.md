---
label: Mua mail
icon: credit-card
order: 70
---

# 🛒 Mua mail

Mua mail tự động qua API.

---

## API Endpoint

+++ GET
```
https://emailsieure.com/api/v1/orders/buy?productId=PRODUCT_ID&quantity=QUANTITY&apikey=YOUR_API_KEY
```
+++

---

## Tham số truy vấn

| Tham số | Kiểu | Required | Mô tả |
|---------|------|----------|-------|
| `apikey` | string | :icon-check-circle: Required | API Key của bạn |
| `productId` | number | :icon-check-circle: Required | ID sản phẩm, lấy từ [Danh sách sản phẩm](/lay-danh-sach-san-pham) |
| `quantity` | number | :icon-check-circle: Required | Số lượng cần mua |

---

## Ví dụ

```
https://emailsieure.com/api/v1/orders/buy?productId=1&quantity=2&apikey=YOUR_API_KEY
```

---

## Response thành công

```json
{
    "success": true,
    "message": "Mua hàng thành công",
    "data": {
        "accountData": [
            "eadmund_314ermanno207@outlook.com|iu2QJk894g8Y|mkt=us-EN; mkt1=us-EN; amsc=...",
            "rolliefz5wlnt@outlook.com|XGvznvCEdV2wpY|mkt=us-EN; mkt1=us-EN; amsc=..."
        ]
    },
    "timestamp": "2026-01-04T15:40:30.065211339"
}
```

---

## Giải thích Response

| Field | Kiểu | Mô tả |
|-------|------|-------|
| `success` | boolean | Trạng thái thành công |
| `message` | string | Thông báo |
| `data.accountData` | array | Danh sách tài khoản đã mua |

---

## Định dạng tài khoản

Mỗi tài khoản có định dạng:
```
email|password|refresh_token|client_id
```

---

## Response thất bại

```json
{
    "success": false,
    "message": "Tồn kho không đủ",
    "errorCode": "1111",
    "timestamp": "2026-01-06T21:52:16.6720379"
}
```

---

## Lỗi thường gặp

| errorCode  | message |
|-------|-------------|
| `1111` | Tồn kho không đủ |
| `10003` | Số dư ví không đủ |
| `401` | Chưa xác thực(sai api key) |
