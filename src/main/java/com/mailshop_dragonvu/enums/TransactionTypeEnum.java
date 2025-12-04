package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Transaction Type Enum
 */
@Getter
@RequiredArgsConstructor
public enum TransactionTypeEnum implements BaseEnum {

    DEPOSIT(0, "Nạp tiền vào ví"),
    PURCHASE(1, "Mua tài khoản "),
    REFUND(2, "Hoàn tiền"),
    ADMIN_ADJUST(3, "Admin điều chỉnh số dư");

    @JsonValue
    private final Integer key;
    private final String value;

    @Override
    public Integer getKey() {
        return key;
    }

    /** Converter cho JPA */
    @Converter(autoApply = true)
    public static class TransactionTypeConverter
            extends AbstractBaseEnumConverter<TransactionTypeEnum> {
        public TransactionTypeConverter() {
            super(TransactionTypeEnum.class);
        }
    }

    /** Lấy enum từ key DB */
    public static TransactionTypeEnum fromKey(Integer key) {
        if (key == null) return null;

        for (TransactionTypeEnum type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Không có TransactionType tương ứng với key: " + key
        );
    }
}
