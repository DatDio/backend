package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.Order;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderStatus = :status")
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user")
    Long countByUser(User user);

}
