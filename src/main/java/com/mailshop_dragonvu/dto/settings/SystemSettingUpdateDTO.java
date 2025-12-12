package com.mailshop_dragonvu.dto.settings;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettingUpdateDTO {

    @Size(max = 500, message = "Value không được quá 500 ký tự")
    private String settingValue;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String description;
}
