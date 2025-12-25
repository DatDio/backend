package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;  // Ví dụ: Hotmail New, Outlook Trusted

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String liveTime;

    @Column(length = 2000)
    private String country;

    @Column(nullable = false)
    private Long price;   // Giá 1 tài khoản

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ActiveStatusEnum status = ActiveStatusEnum.ACTIVE;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Số lượng tối thiểu trong kho phụ (trigger chuyển kho khi dưới mức này)
    @Column(name = "min_secondary_stock")
    @Builder.Default
    private Integer minSecondaryStock = 500;

    // Số lượng tối đa trong kho phụ (giới hạn hiển thị cho khách hàng)
    @Column(name = "max_secondary_stock")
    @Builder.Default
    private Integer maxSecondaryStock = 1000;

    // Thời gian hết hạn của mail (tính bằng giờ, 0 = không hết hạn)
    // Ví dụ: Hotmail New = 3 giờ (mail chỉ sống ~5h từ lúc reg)
    @Column(name = "expiration_hours")
    @Builder.Default
    private Integer expirationHours = 0;
}

