package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.entity.OrderEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    OrderEntity toEntity(OrderCreateDTO request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "orderStatus", expression = "java(orderEntity.getOrderStatus().name())")
    OrderResponseDTO toResponse(OrderEntity orderEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    void updateEntity(@MappingTarget OrderEntity orderEntity, OrderUpdateDTO request);
}
