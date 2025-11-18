package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.dto.orders.OrderCreateRequest;
import com.mailshop_dragonvu.dto.orders.OrderResponse;
import com.mailshop_dragonvu.dto.orders.OrderUpdateRequest;
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
    date = "2025-11-18T16:28:06+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public Order toEntity(OrderCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        order.discountAmount( request.getDiscountAmount() );
        order.notes( request.getNotes() );

        return order.build();
    }

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.userId( orderUserId( order ) );
        orderResponse.userEmail( orderUserEmail( order ) );
        orderResponse.id( order.getId() );
        orderResponse.orderNumber( order.getOrderNumber() );
        orderResponse.totalAmount( order.getTotalAmount() );
        orderResponse.discountAmount( order.getDiscountAmount() );
        orderResponse.finalAmount( order.getFinalAmount() );
        orderResponse.notes( order.getNotes() );
        orderResponse.cancelledDate( order.getCancelledDate() );
        orderResponse.cancellationReason( order.getCancellationReason() );
        orderResponse.orderItems( orderItemListToOrderItemResponseList( order.getOrderItems() ) );
        orderResponse.createdAt( order.getCreatedAt() );
        orderResponse.updatedAt( order.getUpdatedAt() );

        orderResponse.orderStatus( order.getOrderStatus().name() );

        return orderResponse.build();
    }

    @Override
    public void updateEntity(Order order, OrderUpdateRequest request) {
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
