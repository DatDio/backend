package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.entity.Order;
import com.mailshop_dragonvu.entity.OrderItem;
import com.mailshop_dragonvu.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T18:10:43+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public Order toEntity(OrderCreateDTO request) {
        if ( request == null ) {
            return null;
        }

        Order.OrderBuilder<?, ?> order = Order.builder();

        order.discountAmount( request.getDiscountAmount() );
        order.notes( request.getNotes() );

        return order.build();
    }

    @Override
    public OrderResponseDTO toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponseDTO.OrderResponseDTOBuilder orderResponseDTO = OrderResponseDTO.builder();

        orderResponseDTO.userId( orderUserId( order ) );
        orderResponseDTO.userEmail( orderUserEmail( order ) );
        orderResponseDTO.id( order.getId() );
        orderResponseDTO.orderNumber( order.getOrderNumber() );
        orderResponseDTO.totalAmount( order.getTotalAmount() );
        orderResponseDTO.discountAmount( order.getDiscountAmount() );
        orderResponseDTO.finalAmount( order.getFinalAmount() );
        orderResponseDTO.notes( order.getNotes() );
        orderResponseDTO.cancelledDate( order.getCancelledDate() );
        orderResponseDTO.cancellationReason( order.getCancellationReason() );
        orderResponseDTO.orderItems( orderItemListToOrderItemResponseList( order.getOrderItems() ) );
        orderResponseDTO.createdAt( order.getCreatedAt() );
        orderResponseDTO.updatedAt( order.getUpdatedAt() );

        orderResponseDTO.orderStatus( order.getOrderStatus().name() );

        return orderResponseDTO.build();
    }

    @Override
    public void updateEntity(Order order, OrderUpdateDTO request) {
        if ( request == null ) {
            return;
        }

        if ( request.getNotes() != null ) {
            order.setNotes( request.getNotes() );
        }
    }

    private Long orderUserId(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderUserEmail(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    protected List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( orderItemMapper.toResponse( orderItem ) );
        }

        return list1;
    }
}
