package com.mailshop_dragonvu.dto.ranks;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

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

    @Min(value = 1, message = "Số ngày tính hạng phải >= 1")
    private Integer periodDays;

    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder;

    private String iconUrl;

    private String color;

    private Integer status;
}
