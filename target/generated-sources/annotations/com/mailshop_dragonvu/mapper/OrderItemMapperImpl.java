package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orderitems.OrderItemRequest;
import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.entity.OrderItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-18T16:28:06+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItem toEntity(OrderItemRequest request) {
        if ( request == null ) {
            return null;
        }

        OrderItem.OrderItemBuilder orderItem = OrderItem.builder();

        orderItem.productId( request.getProductId() );
        orderItem.productName( request.getProductName() );
        orderItem.quantity( request.getQuantity() );
        orderItem.discountAmount( request.getDiscountAmount() );
        orderItem.notes( request.getNotes() );

        return orderItem.build();
    }

    @Override
    public OrderItemResponse toResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.id( orderItem.getId() );
        orderItemResponse.productId( orderItem.getProductId() );
        orderItemResponse.productName( orderItem.getProductName() );
        orderItemResponse.quantity( orderItem.getQuantity() );
        orderItemResponse.discountAmount( orderItem.getDiscountAmount() );
        orderItemResponse.totalPrice( orderItem.getTotalPrice() );
        orderItemResponse.notes( orderItem.getNotes() );
        orderItemResponse.createdAt( orderItem.getCreatedAt() );

        return orderItemResponse.build();
    }

    @Override
    public void updateEntity(OrderItem orderItem, OrderItemRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getProductId() != null ) {
            orderItem.setProductId( request.getProductId() );
        }
        if ( request.getProductName() != null ) {
            orderItem.setProductName( request.getProductName() );
        }
        if ( request.getQuantity() != null ) {
            orderItem.setQuantity( request.getQuantity() );
        }
        if ( request.getDiscountAmount() != null ) {
            orderItem.setDiscountAmount( request.getDiscountAmount() );
        }
        if ( request.getNotes() != null ) {
            orderItem.setNotes( request.getNotes() );
        }
    }
}
