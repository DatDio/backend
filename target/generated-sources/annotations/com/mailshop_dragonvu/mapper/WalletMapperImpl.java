package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.response.WalletResponse;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.entity.Wallet;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-17T22:47:05+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class WalletMapperImpl implements WalletMapper {

    @Override
    public WalletResponse toResponse(Wallet wallet) {
        if ( wallet == null ) {
            return null;
        }

        WalletResponse.WalletResponseBuilder walletResponse = WalletResponse.builder();

        walletResponse.userId( walletUserId( wallet ) );
        walletResponse.id( wallet.getId() );
        walletResponse.balance( wallet.getBalance() );
        walletResponse.totalDeposited( wallet.getTotalDeposited() );
        walletResponse.totalSpent( wallet.getTotalSpent() );
        walletResponse.isLocked( wallet.getIsLocked() );
        walletResponse.lockReason( wallet.getLockReason() );

        return walletResponse.build();
    }

    private Long walletUserId(Wallet wallet) {
        if ( wallet == null ) {
            return null;
        }
        User user = wallet.getUser();
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
