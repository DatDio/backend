package com.mailshop_dragonvu.dto.users;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

@Data
public class UserFilterDTO extends PageFilterDTO {
    private Long id;

    private String email;

    private String password;

    private String fullName;

    private String status;
}
