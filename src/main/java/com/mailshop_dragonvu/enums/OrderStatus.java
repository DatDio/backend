package com.mailshop_dragonvu.enums;

/**
 * Order Status for Digital Products (Mail Accounts)
 * Simplified - no shipping/delivery needed
 */
public enum OrderStatus {
    PENDING,        // Đang chờ thanh toán
    PAID,           // Đã thanh toán, chờ xử lý
    COMPLETED,      // Đã giao tài khoản (instant delivery)
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}
