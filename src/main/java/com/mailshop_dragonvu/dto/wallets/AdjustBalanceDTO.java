package com.mailshop_dragonvu.dto.wallets;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustBalanceDTO {

    @NotNull(message = "Số tiền không được để trống")
    private Long amount; // + để cộng, - để trừ

    private String reason;
}
