---
label: Lấy danh sách sản phẩm
icon: package
order: 80
---

# 📦 Lấy danh sách sản phẩm

Lấy thông tin tất cả sản phẩm đang được bán.

---

## API Endpoint

+++ GET
```
https://emailsieure.com/api/v1/products/get-all
```
+++

!!!info Không cần API Key
API này không yêu cầu xác thực, có thể gọi trực tiếp.
!!!

---

## Response

```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "name": "HotMail New",
            "description": null,
            "price": 50,
            "liveTime": "3-5 giờ",
            "country": "VN",
            "imageUrl": "https://emailsieure.com/uploads/products/c39e1e78.jpg",
            "quantity": 907
        },
        {
            "id": 2,
            "name": "Mail Test",
            "description": null,
            "price": 50,
            "liveTime": "3-5 giờ",
            "country": null,
            "imageUrl": null,
            "quantity": 10
        }
    ],
    "timestamp": "2026-01-04T22:32:32.8576455"
}
```

---

## Giải thích Response

| Field | Kiểu | Mô tả |
|-------|------|-------|
| `id` | number | ID sản phẩm (dùng khi mua) |
| `name` | string | Tên sản phẩm |
| `description` | string | Mô tả sản phẩm |
| `price` | number | Giá mỗi mail (VNĐ) |
| `liveTime` | string | Thời gian sống của mail |
| `country` | string | Quốc gia |
| `imageUrl` | string | Link ảnh sản phẩm |
| `quantity` | number | Số lượng còn trong kho |
