package com.mailshop_dragonvu.dto.orders;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFilterDTO extends PageFilterDTO {
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private String orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
