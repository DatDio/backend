package com.mailshop_dragonvu.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    private String avatarUrl;

    private Integer status; // 0: INACTIVE, 1: ACTIVE

    private List<String> roles; // List of role names (e.g., ["ADMIN", "USER"])

    private Boolean isCollaborator;

    @Min(value = 0, message = "Phần trăm bonus không được nhỏ hơn 0")
    @Max(value = 100, message = "Phần trăm bonus không được lớn hơn 100")
    private Integer bonusPercent;
}
