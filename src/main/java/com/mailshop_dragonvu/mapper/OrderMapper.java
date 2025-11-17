package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.request.OrderCreateRequest;
import com.mailshop_dragonvu.dto.request.OrderUpdateRequest;
import com.mailshop_dragonvu.dto.response.OrderResponse;
import com.mailshop_dragonvu.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    Order toEntity(OrderCreateRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "orderStatus", expression = "java(order.getOrderStatus().name())")
    OrderResponse toResponse(Order order);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    void updateEntity(@MappingTarget Order order, OrderUpdateRequest request);

}
