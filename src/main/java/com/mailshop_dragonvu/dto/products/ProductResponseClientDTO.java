package com.mailshop_dragonvu.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseClientDTO {
    private Long id;

    private String name;

    private String description;

    private Long price;

    private String liveTime;

    private String country;

    private String imageUrl;

    // Số lượng còn lại trong kho PHỤ (hiển thị cho khách hàng)
    private Long quantity;

}
