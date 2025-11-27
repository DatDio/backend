package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.orders.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    ClientOrderCreateResponseDTO createOrder(OrderCreateDTO request, Long userId);

    OrderResponseDTO getOrderById(Long id, Long userId);

    Page<OrderResponseDTO> search(OrderFilterDTO orderFilterDTO);

    Page<OrderResponseDTO> getOrdersByUser(Long userId, Pageable pageable);

    void deleteOrder(Long id);

}
