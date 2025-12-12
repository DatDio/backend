package com.mailshop_dragonvu.dto.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateDTO {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @Size(max = 255, message = "Mô tả không được quá 255 kí tự")
    private String description;

    private MultipartFile image;

    private Integer status;
}
