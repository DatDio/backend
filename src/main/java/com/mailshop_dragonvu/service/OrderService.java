package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDTO createOrder(OrderCreateDTO request, Long userId);

    OrderResponseDTO updateOrder(Long id, OrderUpdateDTO request, Long userId);

    OrderResponseDTO getOrderById(Long id, Long userId);

    OrderResponseDTO getOrderByNumber(String orderNumber, Long userId);

    Page<OrderResponseDTO> getAllOrders(Pageable pageable);

    Page<OrderResponseDTO> getOrdersByUser(Long userId, Pageable pageable);

    Page<OrderResponseDTO> getOrdersByStatus(OrderStatus status, Pageable pageable);

    OrderResponseDTO updateOrderStatus(Long id, OrderStatus status);

    OrderResponseDTO confirmOrder(Long id);

    OrderResponseDTO cancelOrder(Long id, String reason, Long userId);

    void deleteOrder(Long id);

}
