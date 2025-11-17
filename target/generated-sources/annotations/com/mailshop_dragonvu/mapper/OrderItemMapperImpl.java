package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.request.OrderItemRequest;
import com.mailshop_dragonvu.dto.response.OrderItemResponse;
import com.mailshop_dragonvu.entity.OrderItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-17T22:47:05+0700",
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
        orderItem.productSku( request.getProductSku() );
        orderItem.quantity( request.getQuantity() );
        orderItem.unitPrice( request.getUnitPrice() );
        orderItem.discountAmount( request.getDiscountAmount() );
        orderItem.taxAmount( request.getTaxAmount() );
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
        orderItemResponse.productSku( orderItem.getProductSku() );
        orderItemResponse.quantity( orderItem.getQuantity() );
        orderItemResponse.unitPrice( orderItem.getUnitPrice() );
        orderItemResponse.discountAmount( orderItem.getDiscountAmount() );
        orderItemResponse.taxAmount( orderItem.getTaxAmount() );
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
        if ( request.getProductSku() != null ) {
            orderItem.setProductSku( request.getProductSku() );
        }
        if ( request.getQuantity() != null ) {
            orderItem.setQuantity( request.getQuantity() );
        }
        if ( request.getUnitPrice() != null ) {
            orderItem.setUnitPrice( request.getUnitPrice() );
        }
        if ( request.getDiscountAmount() != null ) {
            orderItem.setDiscountAmount( request.getDiscountAmount() );
        }
        if ( request.getTaxAmount() != null ) {
            orderItem.setTaxAmount( request.getTaxAmount() );
        }
        if ( request.getNotes() != null ) {
            orderItem.setNotes( request.getNotes() );
        }
    }
}
