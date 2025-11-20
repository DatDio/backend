package com.mailshop_dragonvu.dto.products;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import lombok.*;
import java.math.BigDecimal;

@Data
public class ProductFilterDTO extends PageFilterDTO {
    private String name;
    private Long categoryId;
    private Long minPrice;
    private Long maxPrice;
    private String status;
    private Integer minStock;
}
