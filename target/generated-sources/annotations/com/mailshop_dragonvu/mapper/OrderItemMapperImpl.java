package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orderitems.OrderItemRequest;
import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T17:00:06+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItemEntity toEntity(OrderItemRequest request) {
        if ( request == null ) {
            return null;
        }

        OrderItemEntity.OrderItemEntityBuilder<?, ?> orderItemEntity = OrderItemEntity.builder();

        return orderItemEntity.build();
    }

    @Override
    public OrderItemResponse toResponse(OrderItemEntity orderItemEntity) {
        if ( orderItemEntity == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.id( orderItemEntity.getId() );

        return orderItemResponse.build();
    }

    @Override
    public void updateEntity(OrderItemEntity orderItemEntity, OrderItemRequest request) {
        if ( request == null ) {
            return;
        }
    }
}
