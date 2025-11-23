package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-23T12:23:03+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public OrderEntity toEntity(OrderCreateDTO request) {
        if ( request == null ) {
            return null;
        }

        OrderEntity.OrderEntityBuilder<?, ?> orderEntity = OrderEntity.builder();

        return orderEntity.build();
    }

    @Override
    public OrderResponseDTO toResponse(OrderEntity orderEntity) {
        if ( orderEntity == null ) {
            return null;
        }

        OrderResponseDTO.OrderResponseDTOBuilder orderResponseDTO = OrderResponseDTO.builder();

        orderResponseDTO.userId( orderEntityUserId( orderEntity ) );
        orderResponseDTO.userEmail( orderEntityUserEmail( orderEntity ) );
        orderResponseDTO.id( orderEntity.getId() );
        orderResponseDTO.orderNumber( orderEntity.getOrderNumber() );
        if ( orderEntity.getTotalAmount() != null ) {
            orderResponseDTO.totalAmount( BigDecimal.valueOf( orderEntity.getTotalAmount() ) );
        }
        orderResponseDTO.notes( orderEntity.getNotes() );
        orderResponseDTO.orderItems( orderItemEntityListToOrderItemResponseList( orderEntity.getOrderItems() ) );
        orderResponseDTO.createdAt( orderEntity.getCreatedAt() );
        orderResponseDTO.updatedAt( orderEntity.getUpdatedAt() );

        orderResponseDTO.orderStatus( orderEntity.getOrderStatus().name() );

        return orderResponseDTO.build();
    }

    @Override
    public void updateEntity(OrderEntity orderEntity, OrderUpdateDTO request) {
        if ( request == null ) {
            return;
        }

        if ( request.getNotes() != null ) {
            orderEntity.setNotes( request.getNotes() );
        }
    }

    private Long orderEntityUserId(OrderEntity orderEntity) {
        if ( orderEntity == null ) {
            return null;
        }
        UserEntity user = orderEntity.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderEntityUserEmail(OrderEntity orderEntity) {
        if ( orderEntity == null ) {
            return null;
        }
        UserEntity user = orderEntity.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    protected List<OrderItemResponse> orderItemEntityListToOrderItemResponseList(List<OrderItemEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItemEntity orderItemEntity : list ) {
            list1.add( orderItemMapper.toResponse( orderItemEntity ) );
        }

        return list1;
    }
}
