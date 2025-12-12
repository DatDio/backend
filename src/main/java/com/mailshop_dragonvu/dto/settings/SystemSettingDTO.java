package com.mailshop_dragonvu.dto.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettingDTO {

    private Long id;

    @NotBlank(message = "Key không được để trống")
    @Size(max = 100, message = "Key không được quá 100 ký tự")
    private String settingKey;

    @Size(max = 500, message = "Value không được quá 500 ký tự")
    private String settingValue;

    @Size(max = 255, message = "Mô tả không được quá 255 ký tự")
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
