package com.mailshop_dragonvu.dto.ranks;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class RankFilterDTO extends PageFilterDTO {

    private String name;
    private String status;
}

