package com.mailshop_dragonvu.enums;

/**
 * Transaction Status Enum
 */
public enum TransactionStatus {
    PENDING,        // Đang chờ xử lý
    PROCESSING,     // Đang xử lý thanh toán
    SUCCESS,        // Giao dịch thành công
    FAILED,         // Giao dịch thất bại
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}
