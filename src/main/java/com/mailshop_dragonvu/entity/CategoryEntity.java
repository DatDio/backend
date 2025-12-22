package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity extends BaseEntity {
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "status", length = 20, nullable = false)
    private ActiveStatusEnum status = ActiveStatusEnum.ACTIVE;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Builder.Default
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<ProductEntity> products = new ArrayList<>();
}
