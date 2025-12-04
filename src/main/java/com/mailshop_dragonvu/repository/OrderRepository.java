package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    Page<OrderEntity> findByUser(UserEntity userEntity, Pageable pageable);

    Page<OrderEntity> findByOrderStatus(OrderStatusEnum status, Pageable pageable);

    /**
     * Find orders created before the specified date (for cleanup)
     */
    List<OrderEntity> findByCreatedAtBefore(LocalDateTime cutoffDate);
}
