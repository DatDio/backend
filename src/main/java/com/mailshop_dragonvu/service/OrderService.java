package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.request.OrderCreateRequest;
import com.mailshop_dragonvu.dto.request.OrderUpdateRequest;
import com.mailshop_dragonvu.dto.response.OrderResponse;
import com.mailshop_dragonvu.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request, Long userId);

    OrderResponse updateOrder(Long id, OrderUpdateRequest request, Long userId);

    OrderResponse getOrderById(Long id, Long userId);

    OrderResponse getOrderByNumber(String orderNumber, Long userId);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    OrderResponse confirmOrder(Long id);

    OrderResponse shipOrder(Long id);

    OrderResponse deliverOrder(Long id);

    OrderResponse cancelOrder(Long id, String reason, Long userId);

    void deleteOrder(Long id);

}
