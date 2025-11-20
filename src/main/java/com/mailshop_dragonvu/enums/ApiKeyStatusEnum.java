package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API Key Status Enum
 */
@Getter
@RequiredArgsConstructor
public enum ApiKeyStatusEnum implements BaseEnum {

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
    public static class ApiKeyStatusConverter
            extends AbstractBaseEnumConverter<ApiKeyStatusEnum> {
        public ApiKeyStatusConverter() {
            super(ApiKeyStatusEnum.class);
        }
    }

    /**
     * Lấy enum từ database key
     */
    public static ApiKeyStatusEnum fromKey(Integer key) {
        if (key == null) return null;

        for (ApiKeyStatusEnum st : values()) {
            if (st.key.equals(key)) return st;
        }

        throw new IllegalArgumentException(
                "Không có ApiKeyStatus tương ứng với key: " + key
        );
    }
}
