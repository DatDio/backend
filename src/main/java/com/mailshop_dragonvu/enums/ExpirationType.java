package com.mailshop_dragonvu.enums;

import lombok.Getter;

/**
 * Loại thời gian hết hạn cho mail
 */
@Getter
public enum ExpirationType {
    NONE(0, "Không hết hạn"),
    HOURS_3(3, "3 giờ"),
    HOURS_6(6, "6 giờ"),
    MONTH_1(720, "1 tháng"),      // 30 ngày * 24 giờ
    MONTH_6(4320, "6 tháng");     // 180 ngày * 24 giờ

    private final int hours;
    private final String label;

    ExpirationType(int hours, String label) {
        this.hours = hours;
        this.label = label;
    }

    /**
     * Tìm ExpirationType từ số giờ
     */
    public static ExpirationType fromHours(Integer hours) {
        if (hours == null || hours <= 0) {
            return NONE;
        }
        for (ExpirationType type : values()) {
            if (type.hours == hours) {
                return type;
            }
        }
        // Fallback: nếu không khớp chính xác, trả về NONE
        return NONE;
    }
}
