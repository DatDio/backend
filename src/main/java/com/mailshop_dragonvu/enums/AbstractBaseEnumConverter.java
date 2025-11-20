package com.mailshop_dragonvu.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

@Converter
public abstract class AbstractBaseEnumConverter<E extends Enum<E> & BaseEnum>
        implements AttributeConverter<E, Integer> {

    private final Class<E> enumClass;

    protected AbstractBaseEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Integer convertToDatabaseColumn(E attribute) {
        return (attribute == null) ? null : attribute.getKey();
    }

    @Override
    public E convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (E constant : enumClass.getEnumConstants()) {
            if (Objects.equals(constant.getKey(), dbData)) {
                return constant;
            }
        }
        throw new IllegalArgumentException(
                "Không có giá trị " + dbData + " với enum " + enumClass.getSimpleName()
        );
    }
}
