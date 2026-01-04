package com.mailshop_dragonvu.scheduler;

import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler để cập nhật trạng thái hết hạn cho product items
 * Chạy mỗi 5 phút để đánh dấu các items đã hết hạn
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductExpirationScheduler {

    private final ProductItemRepository productItemRepository;

    /**
     * Cập nhật trạng thái hết hạn - chạy mỗi 5 phút
     */
    @Scheduled(fixedRateString = "${app.expiration.interval:300000}") // 5 phút = 300000ms
    @Transactional
    public void updateExpiredItems() {
        log.info("Starting product expiration check...");
        
        try {
            // Tìm tất cả items hết hạn nhưng chưa được đánh dấu
            List<ProductItemEntity> expiredItems = productItemRepository.findAllExpiredItemsToMark();
            
            if (expiredItems.isEmpty()) {
                log.debug("No expired items found");
                return;
            }

            // Đánh dấu từng item là expired
            List<Long> expiredIds = expiredItems.stream()
                    .map(ProductItemEntity::getId)
                    .toList();
            
            productItemRepository.markAsExpired(expiredIds);
            
            log.info("Marked {} items as expired", expiredIds.size());
            
        } catch (Exception e) {
            log.error("Error during expiration check: {}", e.getMessage(), e);
        }
    }

    /**
     * Force check - có thể gọi thủ công qua API
     */
    @Transactional
    public int forceUpdateExpiredItems() {
        List<ProductItemEntity> expiredItems = productItemRepository.findAllExpiredItemsToMark();
        
        if (expiredItems.isEmpty()) {
            return 0;
        }

        List<Long> expiredIds = expiredItems.stream()
                .map(ProductItemEntity::getId)
                .toList();
        
        productItemRepository.markAsExpired(expiredIds);
        
        log.info("Force marked {} items as expired", expiredIds.size());
        return expiredIds.size();
    }
}
