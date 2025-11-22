package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderFilterDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDTO createOrder(OrderCreateDTO request, Long userId);

    OrderResponseDTO getOrderById(Long id, Long userId);

    Page<OrderResponseDTO> search(OrderFilterDTO orderFilterDTO);

    Page<OrderResponseDTO> getOrdersByUser(Long userId, Pageable pageable);

    void deleteOrder(Long id);

}
