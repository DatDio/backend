package com.mailshop_dragonvu.dto.payos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Deposit Request DTO - User nạp tiền vào ví
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotNull(message = "Số tiền không được để trống!")
    @DecimalMin(value = "10000", message = "Số tiền tối thiểu trên 10,000 VND")
    private BigDecimal amount;

    private String description;

    /**
     * Return URL after payment success
     */
    private String returnUrl;

    /**
     * Cancel URL if user cancels payment
     */
    private String cancelUrl;
}
