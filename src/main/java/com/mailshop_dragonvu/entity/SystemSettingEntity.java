package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * System Settings Entity - Key-value store for system configurations
 * Examples: rank.period_days, app.maintenance_mode, etc.
 */
@Entity
@Table(name = "system_settings")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingEntity extends BaseEntity {

    @Column(name = "setting_key", length = 100, nullable = false, unique = true)
    private String settingKey;

    @Column(name = "setting_value", length = 500)
    private String settingValue;

    @Column(name = "description", length = 255)
    private String description;
}
