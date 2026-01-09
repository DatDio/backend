package com.mailshop_dragonvu.utils;

/**
 * Constants for i18n message keys.
 * Use these constants instead of hardcoding strings to make code maintainable.
 * 
 * <p>Usage example:
 * <pre>
 * messageService.getMessage(MessageKeys.Auth.REGISTERED);
 * </pre>
 */
public final class MessageKeys {

    private MessageKeys() {
        // Prevent instantiation
    }

    /**
     * Authentication related messages
     */
    public static final class Auth {
        public static final String REGISTERED = "success.auth.registered";
        public static final String LOGIN = "success.auth.login";
        public static final String PASSWORD_CHANGED = "success.auth.password_changed";
        public static final String GOOGLE_LOGIN = "success.auth.google_login";
        public static final String TOKEN_REFRESHED = "success.auth.token_refreshed";
        public static final String LOGOUT = "success.auth.logout";
        
        private Auth() {}
    }

    /**
     * User related messages
     */
    public static final class User {
        public static final String CREATED = "success.user.created";
        public static final String UPDATED = "success.user.updated";
        public static final String DELETED = "success.user.deleted";
        public static final String BALANCE_ADDED = "success.user.balance_added";
        public static final String BALANCE_DEDUCTED = "success.user.balance_deducted";
        
        private User() {}
    }

    /**
     * Order related messages
     */
    public static final class Order {
        public static final String PURCHASE = "success.order.purchase";
        
        private Order() {}
    }

    /**
     * Transaction & Wallet related messages
     */
    public static final class Transaction {
        public static final String DELETED = "success.transaction.deleted";
        public static final String STATUS_UPDATED = "success.transaction.status_updated";
        
        private Transaction() {}
    }

    /**
     * API Key related messages
     */
    public static final class ApiKey {
        public static final String DELETED = "success.apikey.deleted";
        
        private ApiKey() {}
    }

    /**
     * Product Item related messages
     */
    public static final class Items {
        public static final String ADDED = "success.items.added";
        public static final String ADDED_WITH_DUPLICATES = "success.items.added_with_duplicates";
        public static final String IMPORTED = "success.items.imported";
        public static final String DELETED = "success.items.deleted";
        public static final String BULK_DELETED = "success.items.bulk_deleted";
        public static final String EXPIRED_DELETED = "success.items.expired_deleted";
        
        private Items() {}
    }

    /**
     * Category related messages
     */
    public static final class Category {
        public static final String CREATED = "success.category.created";
        public static final String UPDATED = "success.category.updated";
        public static final String DELETED = "success.category.deleted";
        
        private Category() {}
    }

    /**
     * Rank related messages
     */
    public static final class Rank {
        public static final String CREATED = "success.rank.created";
        public static final String UPDATED = "success.rank.updated";
        public static final String DELETED = "success.rank.deleted";
        
        private Rank() {}
    }

    /**
     * Setting related messages
     */
    public static final class Setting {
        public static final String CREATED = "success.setting.created";
        public static final String UPDATED = "success.setting.updated";
        public static final String DELETED = "success.setting.deleted";
        public static final String NOT_FOUND = "common.setting.not_found";
        
        private Setting() {}
    }

    /**
     * Error messages - Captcha
     */
    public static final class Captcha {
        public static final String REQUIRED = "error.captcha.required";
        public static final String VERIFY_FAILED = "error.captcha.verify_failed";
        public static final String FAILED = "error.captcha.failed";
        public static final String SUSPICIOUS = "error.captcha.suspicious";
        public static final String RETRY = "error.captcha.retry";
        
        private Captcha() {}
    }

    /**
     * Error messages - File Upload
     */
    public static final class File {
        public static final String TOO_LARGE = "error.file.too_large";
        public static final String INVALID = "error.file.invalid";
        public static final String IMAGE_ONLY = "error.file.image_only";
        public static final String UPLOAD_FAILED = "error.file.upload_failed";
        
        private File() {}
    }

    /**
     * Error messages - Product
     */
    public static final class Product {
        public static final String EMPTY_CONTENT = "error.product.empty_content";
        public static final String NOT_ENOUGH_STOCK = "error.product.not_enough_stock_msg";
        public static final String FILE_READ_ERROR = "error.product.file_read";
        
        private Product() {}
    }

    /**
     * Notification messages
     */
    public static final class Notification {
        public static final String DEPOSIT_SUCCESS = "notification.deposit.success";
        
        private Notification() {}
    }
}
