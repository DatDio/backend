package com.mailshop_dragonvu.dto.productitems;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

@Data
public class ProductItemFilterDTO extends PageFilterDTO {
    private Long productId;
    private Boolean sold;
    private String accountData;
    
    // Filter theo loại thời gian hết hạn (NONE, HOURS_3, HOURS_6, MONTH_1, MONTH_6)
    private String expirationType;
}


