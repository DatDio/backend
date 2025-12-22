package com.mailshop_dragonvu.dto.transactions;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionFilterDTO extends PageFilterDTO {
    private String transactionCode;
    private String email;           // Tìm theo email user
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Integer status;         // 0=PENDING, 2=SUCCESS, 3=FAILED
    private Integer type;           // 0=DEPOSIT, 1=PURCHASE, 2=REFUND, 3=ADMIN_ADJUST
    private Long userId;
    private Long minAmount;
    private Long maxAmount;
}

