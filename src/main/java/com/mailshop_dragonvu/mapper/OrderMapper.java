package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    /**
     * Convert OrderEntity -> OrderResponseDTO
     */
    public OrderResponseDTO toResponse(OrderEntity orderEntity) {
        if (orderEntity == null) return null;

        return OrderResponseDTO.builder()
                .id(orderEntity.getId())
                .orderNumber(orderEntity.getOrderNumber())

                // User info
                .userId(orderEntity.getUser() != null ? orderEntity.getUser().getId() : null)
                .userEmail(orderEntity.getUser() != null ? orderEntity.getUser().getEmail() : null)

                // Status + tiền
                .orderStatus(orderEntity.getOrderStatus() != null
                        ? orderEntity.getOrderStatus().name()
                        : null)
                .totalAmount(orderEntity.getTotalAmount())

                // Product info (lấy từ item đầu tiên)
                .productId(getProductId(orderEntity))
                .productName(getProductName(orderEntity))

                //  Toàn bộ accountData cho FE hiển thị
                .accountData(mapAccountData(orderEntity))

                .createdAt(orderEntity.getCreatedAt())
                .build();
    }

    // ================= HELPER METHODS =================

    /**
     * Gom toàn bộ accountData từ OrderItem
     */
    private List<String> mapAccountData(OrderEntity orderEntity) {
        if (orderEntity.getOrderItems() == null) {
            return List.of();
        }

        return orderEntity.getOrderItems()
                .stream()
                .filter(item -> item.getProductItem() != null)
                .map(item -> item.getProductItem().getAccountData())
                .toList();
    }

    /**
     * Lấy productId từ item đầu tiên
     */
    private Long getProductId(OrderEntity orderEntity) {
        return orderEntity.getOrderItems()
                .stream()
                .findFirst()
                .map(item -> item.getProductItem().getProduct().getId())
                .orElse(null);
    }

    /**
     * Lấy productName từ item đầu tiên
     */
    private String getProductName(OrderEntity orderEntity) {
        return orderEntity.getOrderItems()
                .stream()
                .findFirst()
                .map(item -> item.getProductItem().getProduct().getName())
                .orElse(null);
    }
}
