package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.order.id = :orderId")
    List<OrderItemEntity> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.productId = :productId")
    List<OrderItemEntity> findByProductId(Long productId);

}
