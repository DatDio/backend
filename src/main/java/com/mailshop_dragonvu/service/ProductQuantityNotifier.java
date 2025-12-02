package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.websocket.ProductQuantityMessage;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQuantityNotifier {

    private static final String DESTINATION = "/topic/product-quantity";

    private final SimpMessagingTemplate messagingTemplate;
    private final ProductItemRepository productItemRepository;

    public void publishAfterCommit(Long productId) {
        if (productId == null) {
            return;
        }
        publishAfterCommit(Set.of(productId));
    }

    public void publishAfterCommit(Collection<Long> productIds) {
        if (productIds == null) {
            return;
        }

        Set<Long> uniqueIds = productIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (uniqueIds.isEmpty()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            uniqueIds.forEach(this::sendQuantity);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                uniqueIds.forEach(ProductQuantityNotifier.this::sendQuantity);
            }
        });
    }

    private void sendQuantity(Long productId) {
        try {
            long quantity = productItemRepository.countAvailableItems(productId);
            ProductQuantityMessage payload = ProductQuantityMessage.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            messagingTemplate.convertAndSend(DESTINATION, payload);
        } catch (Exception ex) {
            log.error("Failed to publish quantity for product {}: {}", productId, ex.getMessage(), ex);
        }
    }
}
