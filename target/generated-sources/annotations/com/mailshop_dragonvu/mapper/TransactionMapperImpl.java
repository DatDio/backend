package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.entity.Transaction;
import com.mailshop_dragonvu.entity.User;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T18:10:42+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponseDTO toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponseDTO.TransactionResponseDTOBuilder transactionResponseDTO = TransactionResponseDTO.builder();

        transactionResponseDTO.userId( transactionUserId( transaction ) );
        if ( transaction.getType() != null ) {
            transactionResponseDTO.type( transaction.getType().name() );
        }
        if ( transaction.getStatus() != null ) {
            transactionResponseDTO.status( transaction.getStatus().name() );
        }
        transactionResponseDTO.id( transaction.getId() );
        transactionResponseDTO.transactionCode( transaction.getTransactionCode() );
        if ( transaction.getAmount() != null ) {
            transactionResponseDTO.amount( BigDecimal.valueOf( transaction.getAmount() ) );
        }
        if ( transaction.getBalanceBefore() != null ) {
            transactionResponseDTO.balanceBefore( BigDecimal.valueOf( transaction.getBalanceBefore() ) );
        }
        if ( transaction.getBalanceAfter() != null ) {
            transactionResponseDTO.balanceAfter( BigDecimal.valueOf( transaction.getBalanceAfter() ) );
        }
        transactionResponseDTO.description( transaction.getDescription() );
        transactionResponseDTO.paymentMethod( transaction.getPaymentMethod() );
        transactionResponseDTO.paymentReference( transaction.getPaymentReference() );
        transactionResponseDTO.createdAt( transaction.getCreatedAt() );
        transactionResponseDTO.completedAt( transaction.getCompletedAt() );

        return transactionResponseDTO.build();
    }

    private Long transactionUserId(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }
        User user = transaction.getUser();
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
