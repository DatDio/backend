package com.mailshop_dragonvu.dto.productitems;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

@Data
public class ProductItemFilterDTO extends PageFilterDTO {
    private Long productId;
    private Boolean sold = false;
}
