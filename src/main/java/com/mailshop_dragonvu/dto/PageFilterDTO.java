package com.mailshop_dragonvu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageFilterDTO {
    private Integer page = 0;
    private Integer limit = 10;
    private String sort;

}