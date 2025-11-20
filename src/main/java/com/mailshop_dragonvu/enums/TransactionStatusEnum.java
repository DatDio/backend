package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Transaction Status Enum
 */
@Getter
@RequiredArgsConstructor
public enum TransactionStatusEnum implements BaseEnum {

    PENDING(0, "Đang chờ xử lý"),
    PROCESSING(1, "Đang xử lý thanh toán"),
    SUCCESS(2, "Giao dịch thành công"),
    FAILED(3, "Giao dịch thất bại"),
    CANCELLED(4, "Đã hủy"),
    REFUNDED(5, "Đã hoàn tiền");

    @JsonValue
    private final Integer key;
    private final String value;

    @Override
    public Integer getKey() {
        return key;
    }

    /** Converter cho JPA */
    @Converter(autoApply = true)
    public static class TransactionStatusConverter
            extends AbstractBaseEnumConverter<TransactionStatusEnum> {
        public TransactionStatusConverter() {
            super(TransactionStatusEnum.class);
        }
    }

    /** Lấy enum theo key từ DB */
    public static TransactionStatusEnum fromKey(Integer key) {
        if (key == null) return null;

        for (TransactionStatusEnum st : values()) {
            if (st.key.equals(key)) return st;
        }

        throw new IllegalArgumentException(
                "Không có TransactionStatus tương ứng với key: " + key
        );
    }
}
