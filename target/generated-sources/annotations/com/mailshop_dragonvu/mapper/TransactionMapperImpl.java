package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.response.TransactionResponse;
import com.mailshop_dragonvu.entity.Transaction;
import com.mailshop_dragonvu.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-17T22:47:05+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponse toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponse.TransactionResponseBuilder transactionResponse = TransactionResponse.builder();

        transactionResponse.userId( transactionUserId( transaction ) );
        if ( transaction.getType() != null ) {
            transactionResponse.type( transaction.getType().name() );
        }
        if ( transaction.getStatus() != null ) {
            transactionResponse.status( transaction.getStatus().name() );
        }
        transactionResponse.id( transaction.getId() );
        transactionResponse.transactionCode( transaction.getTransactionCode() );
        transactionResponse.amount( transaction.getAmount() );
        transactionResponse.balanceBefore( transaction.getBalanceBefore() );
        transactionResponse.balanceAfter( transaction.getBalanceAfter() );
        transactionResponse.description( transaction.getDescription() );
        transactionResponse.paymentMethod( transaction.getPaymentMethod() );
        transactionResponse.paymentReference( transaction.getPaymentReference() );
        transactionResponse.createdAt( transaction.getCreatedAt() );
        transactionResponse.completedAt( transaction.getCompletedAt() );

        return transactionResponse.build();
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
