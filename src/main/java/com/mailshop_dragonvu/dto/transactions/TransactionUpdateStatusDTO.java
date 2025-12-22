package com.mailshop_dragonvu.dto.transactions;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateStatusDTO {

    @NotNull(message = "Trạng thái mới không được để trống")
    private Integer status; // 2 = SUCCESS, 3 = FAILED

    private String reason; // Lý do (bắt buộc nếu FAILED)
}
