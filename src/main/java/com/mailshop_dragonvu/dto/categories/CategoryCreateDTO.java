package com.mailshop_dragonvu.dto.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateDTO {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @Size(max = 255, message = "Mô tả không được quá 255 kí tự")
    private String description;
}
