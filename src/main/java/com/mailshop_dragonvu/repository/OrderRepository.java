package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // ==================== STATISTICS QUERIES ====================

    /**
     * Đếm số đơn hàng COMPLETED trong khoảng thời gian
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.COMPLETED AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng doanh thu trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.COMPLETED AND o.createdAt BETWEEN :startDate AND :endDate")
    Long sumRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm tổng số đơn hàng COMPLETED
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.COMPLETED")
    Long countCompletedOrders();

    /**
     * Tính tổng doanh thu tất cả đơn COMPLETED
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.COMPLETED")
    Long sumTotalRevenue();
}
