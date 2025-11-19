package com.mailshop_dragonvu.dto.orders;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {


    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

}
