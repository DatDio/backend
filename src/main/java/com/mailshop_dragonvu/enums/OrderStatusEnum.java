package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatusEnum implements BaseEnum {

    PENDING(0, "Đang chờ thanh toán"),
    PAID(1, "Đã thanh toán, chờ xử lý"),
    CONFIRMED(2, "Đã xác nhận"),
    COMPLETED(3, "Đã giao tài khoản"),
    CANCELLED(4, "Đã hủy"),
    REFUNDED(5, "Đã hoàn tiền"),
    DELETED(6, "Đã xóa");

    @JsonValue
    private final Integer key;
    private final String value;

    @Override
    public Integer getKey() {
        return key;
    }

    /** Converter JPA */
    @Converter(autoApply = true)
    public static class OrderStatusConverter
            extends AbstractBaseEnumConverter<OrderStatusEnum> {
        public OrderStatusConverter() {
            super(OrderStatusEnum.class);
        }
    }

    /** Convert DB key → Enum */
    public static OrderStatusEnum fromKey(Integer key) {
        if (key == null) return null;

        for (OrderStatusEnum st : com.mailshop_dragonvu.enums.OrderStatusEnum.values()) {
            if (st.key.equals(key)) return st;
        }

        throw new IllegalArgumentException(
                "Không có OrderStatus tương ứng với key: " + key
        );
    }
}
