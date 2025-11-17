package com.mailshop_dragonvu.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // General Errors (1000-1999)
    INTERNAL_SERVER_ERROR("1000", "Internal server error"),
    INVALID_REQUEST("1001", "Invalid request"),
    RESOURCE_NOT_FOUND("1002", "Resource not found"),
    VALIDATION_ERROR("1003", "Validation error"),
    UNAUTHORIZED("1004", "Unauthorized access"),
    FORBIDDEN("1005", "Forbidden access"),
    BAD_REQUEST("1006", "Bad request"),

    // Authentication Errors (2000-2999)
    INVALID_CREDENTIALS("2000", "Invalid username or password"),
    TOKEN_EXPIRED("2001", "Token has expired"),
    TOKEN_INVALID("2002", "Invalid token"),
    REFRESH_TOKEN_EXPIRED("2003", "Refresh token has expired"),
    REFRESH_TOKEN_INVALID("2004", "Invalid refresh token"),
    ACCOUNT_LOCKED("2005", "Account is locked"),
    ACCOUNT_DISABLED("2006", "Account is disabled"),
    EMAIL_NOT_VERIFIED("2007", "Email not verified"),
    OAUTH2_AUTHENTICATION_FAILED("2008", "OAuth2 authentication failed"),

    // User Errors (3000-3999)
    USER_NOT_FOUND("3000", "User not found"),
    USER_ALREADY_EXISTS("3001", "User already exists"),
    EMAIL_ALREADY_EXISTS("3002", "Email already exists"),
    INVALID_PASSWORD("3003", "Invalid password format"),
    PASSWORD_MISMATCH("3004", "Passwords do not match"),

    // Role & Permission Errors (4000-4999)
    ROLE_NOT_FOUND("4000", "Role not found"),
    ROLE_ALREADY_EXISTS("4001", "Role already exists"),
    PERMISSION_NOT_FOUND("4002", "Permission not found"),
    PERMISSION_DENIED("4003", "Permission denied"),

    // Order Errors (5000-5999)
    ORDER_NOT_FOUND("5000", "Order not found"),
    ORDER_ALREADY_CANCELLED("5001", "Order already cancelled"),
    ORDER_CANNOT_BE_MODIFIED("5002", "Order cannot be modified"),
    INVALID_ORDER_STATUS("5003", "Invalid order status"),
    ORDER_ITEM_NOT_FOUND("5004", "Order item not found"),

    // Invoice Errors (6000-6999)
    INVOICE_NOT_FOUND("6000", "Invoice not found"),
    INVOICE_ALREADY_PAID("6001", "Invoice already paid"),
    INVOICE_ALREADY_CANCELLED("6002", "Invoice already cancelled"),
    INVOICE_GENERATION_FAILED("6003", "Invoice generation failed"),

    // Payment Errors (7000-7999)
    PAYMENT_NOT_FOUND("7000", "Payment not found"),
    PAYMENT_FAILED("7001", "Payment failed"),
    PAYMENT_ALREADY_PROCESSED("7002", "Payment already processed"),
    INVALID_PAYMENT_METHOD("7003", "Invalid payment method"),
    PAYMENT_AMOUNT_MISMATCH("7004", "Payment amount mismatch"),
    MOMO_PAYMENT_ERROR("7010", "MoMo payment error"),
    PAYPAL_PAYMENT_ERROR("7020", "PayPal payment error"),

    // Email Errors (8000-8999)
    EMAIL_SEND_FAILED("8000", "Failed to send email"),
    EMAIL_TEMPLATE_NOT_FOUND("8001", "Email template not found"),
    INVALID_EMAIL_FORMAT("8002", "Invalid email format"),
    EMAIL_NOT_FOUND("8003", "Email log not found"),
    EMAIL_INVALID_STATUS("8004", "Invalid email status"),

    // API Key Errors (9000-9099)
    API_KEY_NOT_FOUND("9000", "API key not found"),
    API_KEY_INVALID("9001", "Invalid API key"),
    API_KEY_EXPIRED("9002", "API key has expired"),
    API_KEY_LIMIT_REACHED("9003", "Maximum API keys limit reached"),
    API_KEY_ALREADY_ACTIVE("9004", "API key is already active"),
    API_KEY_ALREADY_INACTIVE("9005", "API key is already inactive"),

    // Wallet & Transaction Errors (10000-10999)
    WALLET_NOT_FOUND("10000", "Wallet not found"),
    WALLET_ALREADY_EXISTS("10001", "Wallet already exists for this user"),
    WALLET_LOCKED("10002", "Wallet is locked"),
    INSUFFICIENT_BALANCE("10003", "Insufficient wallet balance"),
    TRANSACTION_NOT_FOUND("10004", "Transaction not found"),
    TRANSACTION_ALREADY_PROCESSED("10005", "Transaction already processed"),
    DEPOSIT_AMOUNT_TOO_LOW("10006", "Deposit amount is below minimum"),
    DEPOSIT_AMOUNT_TOO_HIGH("10007", "Deposit amount exceeds maximum"),
    INVALID_AMOUNT_FORMAT("10008", "Invalid amount format"),
    TOO_MANY_PENDING_TRANSACTIONS("10009", "Too many pending transactions"),
    DUPLICATE_TRANSACTION("10010", "Duplicate transaction detected"),
    TRANSACTION_TIMEOUT("10011", "Transaction has timed out"),
    
    // Security Errors (10100-10199)
    RATE_LIMIT_EXCEEDED("10100", "Rate limit exceeded, please try again later"),
    SUSPICIOUS_ACTIVITY("10101", "Suspicious activity detected"),
    IP_BLOCKED("10102", "Your IP address has been blocked"),

    // Cache Errors (9100-9999)
    CACHE_ERROR("9100", "Cache operation failed");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
