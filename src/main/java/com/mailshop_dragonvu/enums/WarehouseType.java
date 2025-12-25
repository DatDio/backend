package com.mailshop_dragonvu.enums;

/**
 * Loại kho lưu trữ mail
 * PRIMARY: Kho chính - không hiển thị cho khách hàng
 * SECONDARY: Kho phụ - hiển thị cho khách hàng, có giới hạn min/max
 */
public enum WarehouseType {
    PRIMARY,    // Kho chính - lưu trữ toàn bộ, không hiển thị
    SECONDARY   // Kho phụ - hiển thị cho khách, có giới hạn
}
