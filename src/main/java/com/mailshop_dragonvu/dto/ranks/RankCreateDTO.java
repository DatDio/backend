package com.mailshop_dragonvu.dto.ranks;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankCreateDTO {

    @NotBlank(message = "Tên thứ hạng không được để trống")
    @Size(max = 50, message = "Tên thứ hạng không được quá 50 ký tự")
    private String name;

    @Min(value = 0, message = "Phần trăm bonus phải >= 0")
    private Integer bonusPercent = 0;

    @Min(value = 0, message = "Mức nạp tối thiểu phải >= 0")
    private Long minDeposit = 0L;

    @Min(value = 1, message = "Số ngày tính hạng phải >= 1")
    private Integer periodDays = 7;

    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder = 0;

    private String iconUrl;

    private String color;
}
