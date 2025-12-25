package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.enums.WarehouseType;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import com.mailshop_dragonvu.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service quản lý logic chuyển kho giữa PRIMARY và SECONDARY
 * 
 * Luồng hoạt động:
 * 1. Upload mail → Tất cả vào kho PRIMARY
 * 2. Khi kho SECONDARY < minSecondaryStock → Chuyển từ PRIMARY sang SECONDARY
 * 3. Số lượng chuyển = maxSecondaryStock - currentSecondary
 * 4. Khách hàng chỉ mua được từ kho SECONDARY
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarehouseService {

    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductQuantityNotifier productQuantityNotifier;

    /**
     * Kiểm tra và chuyển mail từ kho chính sang kho phụ nếu cần
     * Gọi method này sau khi:
     * - Upload mail mới
     * - Sau mỗi lần mua hàng thành công
     */
    public void checkAndTransferStock(Long productId) {
        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            log.warn("Product {} not found for warehouse check", productId);
            return;
        }

        int minStock = product.getMinSecondaryStock() != null ? product.getMinSecondaryStock() : 500;
        int maxStock = product.getMaxSecondaryStock() != null ? product.getMaxSecondaryStock() : 1000;

        long currentSecondary = productItemRepository.countSecondaryItems(productId);

        log.debug("Product {}: Secondary stock = {}, min = {}, max = {}", 
                productId, currentSecondary, minStock, maxStock);

        // Nếu kho phụ dưới mức tối thiểu → chuyển từ kho chính
        if (currentSecondary < minStock) {
            int transferAmount = (int) (maxStock - currentSecondary);
            log.info("Product {}: Secondary stock ({}) < min ({}), need to transfer {} items", 
                    productId, currentSecondary, minStock, transferAmount);
            transferFromPrimaryToSecondary(productId, transferAmount);
        }
    }

    /**
     * Chuyển mail từ kho chính (PRIMARY) sang kho phụ (SECONDARY)
     * 
     * @param productId ID sản phẩm
     * @param quantity Số lượng cần chuyển
     * @return Số lượng thực tế đã chuyển
     */
    public int transferFromPrimaryToSecondary(Long productId, int quantity) {
        if (quantity <= 0) {
            return 0;
        }

        List<ProductItemEntity> items = productItemRepository.findItemsToTransfer(productId, quantity);
        
        if (items.isEmpty()) {
            log.info("Product {}: No items in PRIMARY warehouse to transfer", productId);
            return 0;
        }

        List<Long> ids = items.stream()
                .map(ProductItemEntity::getId)
                .toList();
        
        productItemRepository.updateWarehouseType(ids, WarehouseType.SECONDARY);

        log.info("Product {}: Transferred {} items from PRIMARY to SECONDARY", productId, ids.size());
        
        // Notify WebSocket về thay đổi số lượng
        productQuantityNotifier.publishAfterCommit(productId);

        return ids.size();
    }

    /**
     * Kiểm tra tất cả sản phẩm active và chuyển kho nếu cần
     * Có thể dùng cho manual trigger hoặc batch job
     */
    public void checkAllProducts() {
        List<ProductEntity> products = productRepository.findAllByStatus(ActiveStatusEnum.ACTIVE);
        log.info("Checking warehouse stock for {} active products", products.size());
        
        for (ProductEntity product : products) {
            try {
                checkAndTransferStock(product.getId());
            } catch (Exception e) {
                log.error("Error checking warehouse for product {}: {}", product.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Lấy thông tin kho của sản phẩm
     */
    public WarehouseInfo getWarehouseInfo(Long productId) {
        long primaryCount = productItemRepository.countPrimaryItems(productId);
        long secondaryCount = productItemRepository.countSecondaryItems(productId);
        
        ProductEntity product = productRepository.findById(productId).orElse(null);
        int minStock = product != null && product.getMinSecondaryStock() != null 
                ? product.getMinSecondaryStock() : 500;
        int maxStock = product != null && product.getMaxSecondaryStock() != null 
                ? product.getMaxSecondaryStock() : 1000;

        return new WarehouseInfo(primaryCount, secondaryCount, minStock, maxStock);
    }

    /**
     * DTO chứa thông tin kho
     */
    public record WarehouseInfo(
            long primaryCount,
            long secondaryCount,
            int minSecondaryStock,
            int maxSecondaryStock
    ) {
        public long totalCount() {
            return primaryCount + secondaryCount;
        }
        
        public boolean needsTransfer() {
            return secondaryCount < minSecondaryStock && primaryCount > 0;
        }
    }
}
