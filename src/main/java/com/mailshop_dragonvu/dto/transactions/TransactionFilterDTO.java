package com.mailshop_dragonvu.dto.transactions;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionFilterDTO extends PageFilterDTO {
    private String transactionCode;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
}
