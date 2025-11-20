package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Email Status Enum
 */
@Getter
@RequiredArgsConstructor
public enum EmailStatusEnum implements BaseEnum {

    PENDING(0, "Đang chờ gửi"),
    SENT(1, "Đã gửi"),
    FAILED(2, "Gửi thất bại");

    @JsonValue
    private final Integer key;
    private final String value;

    /**
     * Converter cho JPA
     */
    @Converter(autoApply = true)
    public static class EmailStatusEnumConverter
            extends AbstractBaseEnumConverter<EmailStatusEnum> {
        public EmailStatusEnumConverter() {
            super(EmailStatusEnum.class);
        }
    }

    public static EmailStatusEnum fromKey(Integer key) {
        if (key == null) return null;

        for (EmailStatusEnum status : values()) {
            if (status.key.equals(key)) {
                return status;
            }
        }
        throw new IllegalArgumentException(
                "Không có EmailStatusEnum tương ứng với key: " + key
        );
    }
}
