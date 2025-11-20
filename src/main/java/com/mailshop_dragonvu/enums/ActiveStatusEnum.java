package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActiveStatusEnum implements BaseEnum {

    ACTIVE(1, "Hoạt động"),
    INACTIVE(0, "Không hoạt động");

    @JsonValue
    private final Integer key;
    private final String value;

    @Override
    public Integer getKey() {
        return key;
    }

    /**
     * Converter cho JPA
     */
    @Converter(autoApply = true)
    public static class ActiveStatusEnumConverter
            extends AbstractBaseEnumConverter<ActiveStatusEnum> {
        public ActiveStatusEnumConverter() {
            super(ActiveStatusEnum.class);
        }
    }

    /**
     * Lấy enum từ database key
     */
    public static ActiveStatusEnum fromKey(Integer key) {
        if (key == null) return null;

        for (ActiveStatusEnum st : values()) {
            if (st.key.equals(key)) return st;
        }

        throw new IllegalArgumentException(
                "Không có ActiveStatusEnum tương ứng với key: " + key
        );
    }
}
