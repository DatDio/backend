package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.enums.WarehouseType;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import com.mailshop_dragonvu.repository.ProductRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.ProductItemService;
import com.mailshop_dragonvu.service.ProductQuantityNotifier;
import com.mailshop_dragonvu.service.WarehouseService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductItemServiceImpl implements ProductItemService {

    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductQuantityNotifier productQuantityNotifier;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final WarehouseService warehouseService;

    @Override
    public int batchCreateProductItems(ProductItemCreateDTO productItemCreateDTO) {
        ProductEntity product = productRepository.findById(productItemCreateDTO.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if(!StringUtils.hasText(productItemCreateDTO.getAccountData())){
            throw new BusinessException("Nội dung rỗng!");
        }

        String[] lines = productItemCreateDTO.getAccountData().split("\\r?\\n");
        
        // 1. Deduplicate input
        Set<String> uniqueInputLines = new HashSet<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                uniqueInputLines.add(trimmed);
            }
        }

        if (uniqueInputLines.isEmpty()) {
             return 0;
        }

        // 2. Find existing items in DB
        List<ProductItemEntity> existingItems = productItemRepository.findByProductIdAndAccountDataIn(
                productItemCreateDTO.getProductId(), uniqueInputLines);
        
        Set<String> existingAccounts = existingItems.stream()
                .map(ProductItemEntity::getAccountData)
                .collect(Collectors.toSet());

        // 3. Filter new items (not in DB)
        List<String> newAccountDataList = new ArrayList<>();
        for (String line : uniqueInputLines) {
            if (!existingAccounts.contains(line)) {
                newAccountDataList.add(line);
            }
        }

        // 4. Batch insert using JDBC - Tất cả mail mới vào kho PRIMARY
        if (!newAccountDataList.isEmpty()) {
            Long productId = product.getId();
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            
            // Thêm warehouse_type = 'PRIMARY' khi insert
            String sql = "INSERT INTO product_items (product_id, account_data, sold, warehouse_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.batchUpdate(sql, newAccountDataList, 500, (ps, accountData) -> {
                ps.setLong(1, productId);
                ps.setString(2, accountData);
                ps.setBoolean(3, false);
                ps.setString(4, WarehouseType.PRIMARY.name()); // Tất cả vào kho PRIMARY
                ps.setTimestamp(5, now);
                ps.setTimestamp(6, now);
            });
            
            log.info("Batch inserted {} product items into PRIMARY warehouse for product {}", 
                    newAccountDataList.size(), productId);
            
            productQuantityNotifier.publishAfterCommit(productItemCreateDTO.getProductId());
            
            // Trigger warehouse check - chuyển từ PRIMARY sang SECONDARY nếu cần
            warehouseService.checkAndTransferStock(productId);
        }
        
        // Calculate duplicates count
        long nonEmptyInputCount = 0;
        for(String line : lines) {
            if(!line.trim().isEmpty()) nonEmptyInputCount++;
        }
        
        return (int) (nonEmptyInputCount - newAccountDataList.size());
    }

    @Override
    public Page<ProductItemResponseDTO> searchProductItems(ProductItemFilterDTO productItemFilterDTO) {
        Pageable pageable = PageRequest.of(productItemFilterDTO.getPage(), productItemFilterDTO.getLimit());

        Specification<ProductItemEntity> spec = getSearchSpecification(productItemFilterDTO);

        Page<ProductItemEntity> page = productItemRepository.findAll(spec, pageable);

        return page.map(this::toDTO);

    }

    private Specification<ProductItemEntity> getSearchSpecification(ProductItemFilterDTO req) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter theo productId
            if (req.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), req.getProductId()));
            }

            // 2. Filter theo trạng thái sold
            if (req.getSold() != null) {
                predicates.add(cb.equal(root.get("sold"), req.getSold()));
            }
            if(StringUtils.hasText(req.getAccountData()) ){
                predicates.add(cb.like(cb.lower(root.get("accountData")),
                        "%" + req.getAccountData().trim().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Lấy random items từ kho PHỤ (SECONDARY) để bán
     * Chỉ bán từ kho phụ, không bán từ kho chính
     * Tự động loại bỏ items đã hết hạn trước khi trả về
     */
    @Override
    public List<ProductItemEntity> getRandomUnsoldItems(Long productId, int quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        int expirationHours = product.getExpirationHours() != null ? product.getExpirationHours() : 0;
        
        // 1. Đánh dấu các items đã hết hạn trước khi lấy
        if (expirationHours > 0) {
            markExpiredItems(productId);
        }
        
        // 2. Lấy từ kho SECONDARY (đã loại bỏ expired items trong query)
        List<ProductItemEntity> items =
                productItemRepository.findRandomUnsoldSecondaryItems(productId, quantity);

        if (items.size() < quantity) {
            // Có thể kho phụ không đủ, thử trigger chuyển kho rồi lấy lại
            warehouseService.checkAndTransferStock(productId);
            
            // Đánh dấu expired lần nữa cho items mới chuyển sang
            if (expirationHours > 0) {
                markExpiredItems(productId);
            }
            
            items = productItemRepository.findRandomUnsoldSecondaryItems(productId, quantity);
            
            if (items.size() < quantity) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
            }
        }
        
        // 3. Double-check: lọc ra các items còn hạn (phòng trường hợp race condition)
        if (expirationHours > 0) {
            List<ProductItemEntity> validItems = new ArrayList<>();
            List<Long> expiredIds = new ArrayList<>();
            
            for (ProductItemEntity item : items) {
                if (item.isExpired(expirationHours)) {
                    expiredIds.add(item.getId());
                } else {
                    validItems.add(item);
                }
            }
            
            // Đánh dấu expired cho các items bị lọc ra
            if (!expiredIds.isEmpty()) {
                productItemRepository.markAsExpired(expiredIds);
                log.info("Product {}: Marked {} items as expired during fetch", productId, expiredIds.size());
            }
            
            // Nếu không đủ items sau khi lọc, throw exception
            if (validItems.size() < quantity) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK, 
                    "Không đủ mail còn hạn. Vui lòng thử lại với số lượng ít hơn.");
            }
            
            return validItems;
        }

        return items;
    }
    
    /**
     * Đánh dấu các items đã hết hạn trong database
     */
    private void markExpiredItems(Long productId) {
        List<ProductItemEntity> expiredItems = productItemRepository.findExpiredItems(productId);
        if (!expiredItems.isEmpty()) {
            List<Long> ids = expiredItems.stream()
                    .map(ProductItemEntity::getId)
                    .toList();
            productItemRepository.markAsExpired(ids);
            log.info("Product {}: Marked {} items as expired", productId, ids.size());
            productQuantityNotifier.publishAfterCommit(productId);
        }
    }

    public void markSold(Long itemId, Long buyerId) {
        productItemRepository.markAsSold(itemId, buyerId);
    }
    
    /**
     * Đánh dấu item đã bán và trigger kiểm tra kho
     * Gọi method này sau khi hoàn thành đơn hàng
     */
    public void markSoldAndCheckWarehouse(Long itemId, Long buyerId, Long productId) {
        productItemRepository.markAsSold(itemId, buyerId);
        // Trigger warehouse check sau khi bán
        warehouseService.checkAndTransferStock(productId);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        Optional<ProductItemEntity> itemOptional = productItemRepository.findById(id);
        itemOptional.ifPresent(item -> {
            Long productId = item.getProduct().getId();
            productItemRepository.delete(item);
            productQuantityNotifier.publishAfterCommit(productId);
        });
    }

    @Override
    public void importItems(Long productId, MultipartFile file) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            List<ProductItemEntity> items = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                // Tất cả items import vào kho PRIMARY
                ProductItemEntity item = ProductItemEntity.builder()
                        .product(product)
                        .accountData(trimmed)
                        .sold(false)
                        .warehouseType(WarehouseType.PRIMARY) // Import vào kho PRIMARY
                        .build();

                items.add(item);
            }


            if (!items.isEmpty()) {
                productItemRepository.saveAll(items);
                productQuantityNotifier.publishAfterCommit(productId);
                
                // Trigger warehouse check - chuyển sang SECONDARY nếu cần
                warehouseService.checkAndTransferStock(productId);
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Lỗi đọc file");
        }
    }

    // Convert ENTITY → DTO
    private ProductItemResponseDTO toDTO(ProductItemEntity e) {
        // Get buyer email if buyerId exists
        String buyerEmail = null;
        if (e.getBuyerId() != null) {
            buyerEmail = userRepository.findById(e.getBuyerId())
                    .map(user -> user.getEmail())
                    .orElse(null);
        }
        
        return ProductItemResponseDTO.builder()
                .id(e.getId())
                .productId(e.getProduct().getId())
                .accountData(e.getAccountData())
                .sold(e.getSold())
                .warehouseType(e.getWarehouseType() != null ? e.getWarehouseType().name() : null)
                .buyerId(e.getBuyerId())
                .buyerName(buyerEmail)
                .soldAt(e.getSoldAt() != null ? e.getSoldAt().toString() : null)
                .build();
    }

    // ============ BULK OPERATIONS ============

    @Override
    @Transactional
    public int deleteByAccountData(Long productId, String accountDataList) {
        if (!org.springframework.util.StringUtils.hasText(accountDataList)) {
            return 0;
        }

        // Parse input - mỗi dòng là một account data
        String[] lines = accountDataList.split("\\r?\\n");
        Set<String> accountDataSet = new HashSet<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                accountDataSet.add(trimmed);
            }
        }

        if (accountDataSet.isEmpty()) {
            return 0;
        }

        int deleted = productItemRepository.deleteByAccountDataIn(productId, accountDataSet);
        if (deleted > 0) {
            log.info("Product {}: Deleted {} items by account data", productId, deleted);
            productQuantityNotifier.publishAfterCommit(productId);
        }
        return deleted;
    }

    @Override
    public List<ProductItemResponseDTO> getExpiredItems(Long productId) {
        List<ProductItemEntity> expiredItems = productItemRepository.findAllExpiredItems(productId);
        return expiredItems.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int deleteExpiredItems(Long productId) {
        int deleted = productItemRepository.deleteAllExpiredItems(productId);
        if (deleted > 0) {
            log.info("Product {}: Deleted {} expired items", productId, deleted);
            productQuantityNotifier.publishAfterCommit(productId);
        }
        return deleted;
    }
}

