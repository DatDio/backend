package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.entity.TransactionEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-20T23:43:41+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponseDTO toResponse(TransactionEntity transactionEntity) {
        if ( transactionEntity == null ) {
            return null;
        }

        TransactionResponseDTO.TransactionResponseDTOBuilder transactionResponseDTO = TransactionResponseDTO.builder();

        transactionResponseDTO.userId( transactionEntityUserId( transactionEntity ) );
        if ( transactionEntity.getType() != null ) {
            transactionResponseDTO.type( transactionEntity.getType().name() );
        }
        if ( transactionEntity.getStatus() != null ) {
            transactionResponseDTO.status( transactionEntity.getStatus().name() );
        }
        if ( transactionEntity.getAmount() != null ) {
            transactionResponseDTO.amount( BigDecimal.valueOf( transactionEntity.getAmount() ) );
        }
        if ( transactionEntity.getBalanceAfter() != null ) {
            transactionResponseDTO.balanceAfter( BigDecimal.valueOf( transactionEntity.getBalanceAfter() ) );
        }
        if ( transactionEntity.getBalanceBefore() != null ) {
            transactionResponseDTO.balanceBefore( BigDecimal.valueOf( transactionEntity.getBalanceBefore() ) );
        }
        transactionResponseDTO.completedAt( transactionEntity.getCompletedAt() );
        transactionResponseDTO.createdAt( transactionEntity.getCreatedAt() );
        transactionResponseDTO.description( transactionEntity.getDescription() );
        transactionResponseDTO.id( transactionEntity.getId() );
        transactionResponseDTO.paymentMethod( transactionEntity.getPaymentMethod() );
        transactionResponseDTO.paymentReference( transactionEntity.getPaymentReference() );
        if ( transactionEntity.getTransactionCode() != null ) {
            transactionResponseDTO.transactionCode( String.valueOf( transactionEntity.getTransactionCode() ) );
        }

        return transactionResponseDTO.build();
    }

    private Long transactionEntityUserId(TransactionEntity transactionEntity) {
        if ( transactionEntity == null ) {
            return null;
        }
        UserEntity user = transactionEntity.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
