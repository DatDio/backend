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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ActiveStatusEnum status = ActiveStatusEnum.ACTIVE;
}

