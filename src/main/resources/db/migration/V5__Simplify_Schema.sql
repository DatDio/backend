-- Simplified Database Schema for MailShop DragonVu
-- Chỉ giữ: Users, Roles, Permissions, Wallets, Transactions, Orders, OrderItems

-- Drop old tables if exists
DROP TABLE IF EXISTS INVOICES CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS PAYMENTS CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS EMAIL_LOGS CASCADE CONSTRAINTS;

DROP SEQUENCE IF EXISTS INVOICES_SEQ;
DROP SEQUENCE IF EXISTS PAYMENTS_SEQ;
DROP SEQUENCE IF EXISTS EMAIL_LOGS_SEQ;

-- Update Transactions table - Remove invoice/payment foreign keys
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE TRANSACTIONS DROP CONSTRAINT FK_TRANSACTION_ORDER';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- Update EmailLog table - Remove invoice/payment columns
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE EMAIL_LOGS DROP COLUMN INVOICE_ID';
    EXECUTE IMMEDIATE 'ALTER TABLE EMAIL_LOGS DROP COLUMN PAYMENT_ID';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- Update Orders table - Remove invoice reference if exists
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ORDERS DROP COLUMN INVOICE_ID';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- Comments for simplified schema
COMMENT ON TABLE WALLETS IS 'User wallets - Quản lý số dư người dùng';
COMMENT ON TABLE TRANSACTIONS IS 'Transaction history - Lịch sử nạp tiền và mua hàng';
COMMENT ON TABLE ORDERS IS 'Orders - Đơn hàng mua tài khoản mail (digital product)';
COMMENT ON TABLE ORDER_ITEMS IS 'Order items - Chi tiết sản phẩm trong đơn hàng';

COMMENT ON COLUMN TRANSACTIONS.TYPE IS 'DEPOSIT (nạp tiền) | PURCHASE (mua hàng) | REFUND | ADMIN_ADJUST';
COMMENT ON COLUMN TRANSACTIONS.STATUS IS 'PENDING | PROCESSING | SUCCESS | FAILED | CANCELLED | REFUNDED';
COMMENT ON COLUMN ORDERS.ORDER_STATUS IS 'PENDING (chờ thanh toán) | PAID (đã thanh toán) | COMPLETED (đã giao hàng) | CANCELLED | REFUNDED';

-- Verify simplified structure
SELECT 'Simplified schema migration completed' AS STATUS FROM DUAL;
