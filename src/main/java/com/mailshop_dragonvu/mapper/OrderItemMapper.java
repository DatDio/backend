package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.orderitems.OrderItemRequest;
import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItemRequest request);

    OrderItemResponse toResponse(OrderItemEntity orderItemEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "order", ignore = true)
    void updateEntity(@MappingTarget OrderItemEntity orderItemEntity, OrderItemRequest request);

}
