package com.mailshop_dragonvu.dto.categories;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFilterDTO extends PageFilterDTO {
    private String name;
    private String  status;
}
