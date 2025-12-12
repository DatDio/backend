package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.SystemSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSettingEntity, Long> {

    Optional<SystemSettingEntity> findBySettingKey(String settingKey);

    boolean existsBySettingKey(String settingKey);
}
