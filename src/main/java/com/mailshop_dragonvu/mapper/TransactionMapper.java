package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.transactions.TransactionResponse;
import com.mailshop_dragonvu.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Transaction Mapper
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    TransactionResponse toResponse(Transaction transaction);
}
