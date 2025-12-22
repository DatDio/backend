package com.mailshop_dragonvu.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // General Errors (1000-1999)
    INTERNAL_SERVER_ERROR("1000", "Lỗi máy chủ nội bộ"),
    INVALID_REQUEST("1001", "Yêu cầu không hợp lệ"),
    RESOURCE_NOT_FOUND("1002", "Không tìm thấy tài nguyên"),
    VALIDATION_ERROR("1003", "Lỗi xác thực"),
    UNAUTHORIZED("1004", "Chưa xác thực"),
    FORBIDDEN("1005", "Không có quyền truy cập"),
    BAD_REQUEST("1006", "Yêu cầu không hợp lệ"),
    METHOD_NOT_ALLOWED("1006", "Method không hợp lệ"),

    // Authentication Errors (2000-2999)
    INVALID_CREDENTIALS("2000", "Tên đăng nhập hoặc mật khẩu không hợp lệ"),
    TOKEN_EXPIRED("2001", "Token đã hết hạn"),
    TOKEN_INVALID("2002", "Token không hợp lệ"),
    REFRESH_TOKEN_EXPIRED("2003", "Refresh token đã hết hạn"),
    REFRESH_TOKEN_INVALID("2004", "Refresh token không hợp lệ"),
    ACCOUNT_LOCKED("2005", "Tài khoản đã bị khóa"),
    ACCOUNT_DISABLED("2006", "Tài khoản đã bị vô hiệu hóa"),
    EMAIL_NOT_VERIFIED("2007", "Email chưa được xác minh"),
    OAUTH2_AUTHENTICATION_FAILED("2008", "Xác thực OAuth2 thất bại"),
    INVALID_GOOGLE_TOKEN("2009", "Token Google không hợp lệ"), // Dịch theo key (INVALID_GOOGLE_TOKEN)

    // User Errors (3000-3999)
    USER_NOT_FOUND("3000", "Không tìm thấy người dùng"),
    USER_ALREADY_EXISTS("3001", "Người dùng đã tồn tại"),
    EMAIL_ALREADY_EXISTS("3002", "Email đã tồn tại"),
    INVALID_PASSWORD("3003", "Định dạng mật khẩu không hợp lệ"),
    PASSWORD_MISMATCH("3004", "Mật khẩu không khớp"),

    // Role & Permission Errors (4000-4999)
    ROLE_NOT_FOUND("4000", "Không tìm thấy role"),
    ROLE_ALREADY_EXISTS("4001", "role đã tồn tại"),
    PERMISSION_NOT_FOUND("4002", "Không tìm thấy quyền"),
    PERMISSION_DENIED("4003", "Không có quyền"),

    // Order Errors (5000-5999)
    ORDER_NOT_FOUND("5000", "Không tìm thấy đơn hàng"),
    ORDER_ALREADY_CANCELLED("5001", "Đơn hàng đã bị hủy"),
    ORDER_CANNOT_BE_MODIFIED("5002", "Không thể sửa đổi đơn hàng"),
    INVALID_ORDER_STATUS("5003", "Trạng thái đơn hàng không hợp lệ"),
    ORDER_ITEM_NOT_FOUND("5004", "Không tìm thấy mặt hàng trong đơn hàng"),


    // Payment Errors (7000-7999)
    PAYMENT_NOT_FOUND("7000", "Không tìm thấy thanh toán"),
    PAYMENT_FAILED("7001", "Thanh toán thất bại"),
    PAYMENT_ALREADY_PROCESSED("7002", "Thanh toán đã được xử lý"),
    INVALID_PAYMENT_METHOD("7003", "Phương thức thanh toán không hợp lệ"),
    PAYMENT_AMOUNT_MISMATCH("7004", "Số tiền thanh toán không khớp"),
    PAYMENT_CREATION_FAILED("7021", "Lỗi tạo thanh toán "),
    HMAC_GENERATION_FAILED("7022", "Lỗi tạo thanh toán "),
    INVALID_WEBHOOK("7023", "Lỗi xác thực thanh toán"),
    PAYMENT_CONFIGURATION_ERROR("7024", "Chưa cấu hình thanh toán PayOS"),

    // Email Errors (8000-8999)
    EMAIL_SEND_FAILED("8000", "Gửi email thất bại"),
    EMAIL_TEMPLATE_NOT_FOUND("8001", "Không tìm thấy mẫu email"),
    INVALID_EMAIL_FORMAT("8002", "Định dạng email không hợp lệ"),
    EMAIL_NOT_FOUND("8003", "Không tìm thấy nhật ký email"),
    EMAIL_INVALID_STATUS("8004", "Trạng thái email không hợp lệ"),
    EMAIL_LOG_NOT_FOUND("8005", "Không tìm thấy nhật ký email"),

    // API Key Errors (9000-9099)
    API_KEY_NOT_FOUND("9000", "Không tìm thấy API key"),
    API_KEY_INVALID("9001", "API key không hợp lệ"),
    API_KEY_EXPIRED("9002", "API key đã hết hạn"),
    API_KEY_LIMIT_REACHED("9003", "Đã đạt giới hạn API key tối đa"),
    API_KEY_ALREADY_ACTIVE("9004", "API key đã được kích hoạt"),
    API_KEY_ALREADY_INACTIVE("9005", "API key đã bị vô hiệu hóa"),

    // Wallet & Transaction Errors (10000-10999)
    WALLET_NOT_FOUND("10000", "Không tìm thấy ví"),
    WALLET_ALREADY_EXISTS("10001", "Người dùng này đã có ví"),
    WALLET_LOCKED("10002", "Ví đã bị khóa"),
    INSUFFICIENT_BALANCE("10003", "Số dư ví không đủ"),
    TRANSACTION_NOT_FOUND("10004", "Không tìm thấy giao dịch"),
    TRANSACTION_ALREADY_PROCESSED("10005", "Giao dịch đã được xử lý"),
    DEPOSIT_AMOUNT_TOO_LOW("10006", "Số tiền nạp thấp hơn mức tối thiểu"),
    DEPOSIT_AMOUNT_TOO_HIGH("10007", "Số tiền nạp vượt quá mức tối đa"),
    INVALID_AMOUNT_FORMAT("10008", "Định dạng số tiền không hợp lệ"),
    TOO_MANY_PENDING_TRANSACTIONS("10009", "Có quá nhiều giao dịch đang chờ xử lý"),
    DUPLICATE_TRANSACTION("10010", "Phát hiện giao dịch trùng lặp"),
    TRANSACTION_TIMEOUT("10011", "Giao dịch đã quá hạn"),
    INVALID_AMOUNT("10011", "Amount không hợp lệ"),

    //Product
    PRODUCT_NOT_FOUND("10000", "Không tìm thấy sản phẩm"),
    NOT_ENOUGH_STOCK("1111","Tồn kho không đủ"),

    // Security Errors (10100-10199)
    RATE_LIMIT_EXCEEDED("10100", "Vượt quá giới hạn truy cập, vui lòng thử lại sau"),
    SUSPICIOUS_ACTIVITY("10101", "Phát hiện hoạt động đáng ngờ"),
    IP_BLOCKED("10102", "Địa chỉ IP của bạn đã bị chặn"),

    // Cache Errors (9100-9999)
    CACHE_ERROR("9100", "Lỗi vận hành cache");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}