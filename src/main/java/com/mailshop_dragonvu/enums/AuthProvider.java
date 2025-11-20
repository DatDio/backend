package com.mailshop_dragonvu.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider implements BaseEnum {

    LOCAL(1, "Đăng nhập nội bộ"),
    GOOGLE(2, "Google OAuth");

    @JsonValue
    private final Integer key;
    private final String value;

    @Override
    public Integer getKey() {
        return key;
    }

    @Converter(autoApply = true)
    public static class AuthProviderConverter
            extends AbstractBaseEnumConverter<AuthProvider> {
        public AuthProviderConverter() {
            super(AuthProvider.class);
        }
    }

    public static AuthProvider fromKey(Integer key) {
        if (key == null) return null;

        for (AuthProvider p : values()) {
            if (p.key.equals(key)) return p;
        }

        throw new IllegalArgumentException(
                "Không có AuthProvider tương ứng với key = " + key
        );
    }
}
