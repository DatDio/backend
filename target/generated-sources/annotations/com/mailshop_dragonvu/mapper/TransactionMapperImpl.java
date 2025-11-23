package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.entity.TransactionEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-23T12:23:03+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
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
        transactionResponseDTO.id( transactionEntity.getId() );
        if ( transactionEntity.getTransactionCode() != null ) {
            transactionResponseDTO.transactionCode( String.valueOf( transactionEntity.getTransactionCode() ) );
        }
        if ( transactionEntity.getAmount() != null ) {
            transactionResponseDTO.amount( BigDecimal.valueOf( transactionEntity.getAmount() ) );
        }
        if ( transactionEntity.getBalanceBefore() != null ) {
            transactionResponseDTO.balanceBefore( BigDecimal.valueOf( transactionEntity.getBalanceBefore() ) );
        }
        if ( transactionEntity.getBalanceAfter() != null ) {
            transactionResponseDTO.balanceAfter( BigDecimal.valueOf( transactionEntity.getBalanceAfter() ) );
        }
        transactionResponseDTO.description( transactionEntity.getDescription() );
        transactionResponseDTO.paymentMethod( transactionEntity.getPaymentMethod() );
        transactionResponseDTO.paymentReference( transactionEntity.getPaymentReference() );
        transactionResponseDTO.createdAt( transactionEntity.getCreatedAt() );
        transactionResponseDTO.completedAt( transactionEntity.getCompletedAt() );

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
