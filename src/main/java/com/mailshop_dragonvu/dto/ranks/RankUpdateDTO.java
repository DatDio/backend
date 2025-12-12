package com.mailshop_dragonvu.dto.ranks;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankUpdateDTO {

    @Size(max = 50, message = "Tên thứ hạng không được quá 50 ký tự")
    private String name;

    @Min(value = 0, message = "Phần trăm bonus phải >= 0")
    private Integer bonusPercent;

    @Min(value = 0, message = "Mức nạp tối thiểu phải >= 0")
    private Long minDeposit;

    private MultipartFile icon;

    private String color;

    private Integer status;
}
