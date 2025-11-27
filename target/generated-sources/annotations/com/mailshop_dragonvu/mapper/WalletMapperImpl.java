package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.entity.WalletEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T17:00:06+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class WalletMapperImpl implements WalletMapper {

    @Override
    public WalletResponse toResponse(WalletEntity walletEntity) {
        if ( walletEntity == null ) {
            return null;
        }

        WalletResponse.WalletResponseBuilder walletResponse = WalletResponse.builder();

        walletResponse.userId( walletEntityUserId( walletEntity ) );
        walletResponse.id( walletEntity.getId() );
        walletResponse.balance( walletEntity.getBalance() );
        walletResponse.totalDeposited( walletEntity.getTotalDeposited() );
        walletResponse.totalSpent( walletEntity.getTotalSpent() );
        walletResponse.isLocked( walletEntity.getIsLocked() );
        walletResponse.lockReason( walletEntity.getLockReason() );

        return walletResponse.build();
    }

    private Long walletEntityUserId(WalletEntity walletEntity) {
        if ( walletEntity == null ) {
            return null;
        }
        UserEntity user = walletEntity.getUser();
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
