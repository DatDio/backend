package com.mailshop_dragonvu.dto.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseClientDTO {
    private String email;
    //private String fullName;
    //private String phone;
    //private String address;
    //private String avatarUrl;
    private Long balance; // Wallet balance
    private Long totalDeposit; // Total deposited amount
    private Long totalSpent; // Total spent amount
    private String rankName;
    private Integer bonusPercent;
}
