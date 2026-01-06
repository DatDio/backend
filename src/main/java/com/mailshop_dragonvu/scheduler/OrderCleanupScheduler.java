package com.mailshop_dragonvu.scheduler;

import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.repository.OrderRepository;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import com.mailshop_dragonvu.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled job to cleanup old orders
 * Runs daily at 2:00 AM to delete orders older than configured days
 * Số ngày có thể cấu hình qua Admin Settings (key: scheduler.order_cleanup_days)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    private final OrderRepository orderRepository;
    private final ProductItemRepository productItemRepository;
    private final SystemSettingService systemSettingService;

    // Default: 3 ngày
    private static final int DEFAULT_CLEANUP_DAYS = 3;
    private static final String SETTING_KEY = "scheduler.order_cleanup_days";

    /**
     * Cleanup old orders - runs every day at 2:00 AM
     * Cron: second minute hour day-of-month month day-of-week
     * 
     * This will delete:
     * - OrderEntity
     * - OrderItemEntity (cascade from Order)
     * - ProductItemEntity (explicitly deleted)
     */
    @Scheduled(cron = "${app.order.cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupOldOrders() {
        // Đọc số ngày từ database settings
        int cleanupDays = systemSettingService.getIntValue(SETTING_KEY, DEFAULT_CLEANUP_DAYS);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);

        log.info("Starting order cleanup job. Deleting orders created before: {} ({} days)", cutoffDate, cleanupDays);

        try {
            List<OrderEntity> oldOrders = orderRepository.findByCreatedAtBefore(cutoffDate);

            if (oldOrders.isEmpty()) {
                log.info("No old orders found to delete");
                return;
            }

            int orderCount = oldOrders.size();
            int productItemCount = 0;

            // Collect all ProductItems to delete
            List<ProductItemEntity> productItemsToDelete = new ArrayList<>();
            for (OrderEntity order : oldOrders) {
                for (OrderItemEntity orderItem : order.getOrderItems()) {
                    ProductItemEntity productItem = orderItem.getProductItem();
                    if (productItem != null) {
                        productItemsToDelete.add(productItem);
                    }
                }
            }
            productItemCount = productItemsToDelete.size();

            // IMPORTANT: Delete in correct order to avoid FK constraint violation
            // 1. Delete Orders first (this will cascade delete OrderItems)
            orderRepository.deleteAll(oldOrders);
            orderRepository.flush();
            log.info("Deleted {} orders (OrderItems deleted by cascade)", orderCount);

            // 2. Now delete ProductItems (no longer referenced by OrderItems)
            if (!productItemsToDelete.isEmpty()) {
                productItemRepository.deleteAll(productItemsToDelete);
                productItemRepository.flush();
                log.info("Deleted {} product items", productItemCount);
            }

            log.info("Successfully cleaned up {} orders and {} product items (older than {} days)",
                    orderCount, productItemCount, cleanupDays);

        } catch (Exception e) {
            log.error("Error during order cleanup: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger rollback
        }
    }
}
