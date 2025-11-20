package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.entity.WalletEntity;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-20T23:43:41+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
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
        if ( walletEntity.getBalance() != null ) {
            walletResponse.balance( BigDecimal.valueOf( walletEntity.getBalance() ) );
        }
        walletResponse.id( walletEntity.getId() );
        walletResponse.isLocked( walletEntity.getIsLocked() );
        walletResponse.lockReason( walletEntity.getLockReason() );
        if ( walletEntity.getTotalDeposited() != null ) {
            walletResponse.totalDeposited( BigDecimal.valueOf( walletEntity.getTotalDeposited() ) );
        }
        if ( walletEntity.getTotalSpent() != null ) {
            walletResponse.totalSpent( BigDecimal.valueOf( walletEntity.getTotalSpent() ) );
        }

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
