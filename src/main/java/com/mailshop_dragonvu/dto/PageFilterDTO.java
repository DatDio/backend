package com.mailshop_dragonvu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageFilterDTO {
    private Integer page;
    private Integer limit;
    private String sort;

}