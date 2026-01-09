package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.enums.ExpirationType;
import com.mailshop_dragonvu.enums.WarehouseType;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import com.mailshop_dragonvu.repository.ProductRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.ProductItemService;
import com.mailshop_dragonvu.service.ProductQuantityNotifier;
import com.mailshop_dragonvu.service.WarehouseService;
import com.mailshop_dragonvu.utils.Utils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        if (!StringUtils.hasText(productItemCreateDTO.getAccountData())) {
            throw new BusinessException("Nội dung rỗng!");
        }

        String[] lines = productItemCreateDTO.getAccountData().split("\\r?\\n");

        // 1. Deduplicate input by email - optimized với putIfAbsent
        Map<String, String> emailToAccountData = new LinkedHashMap<>();
        int nonEmptyInputCount = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                nonEmptyInputCount++;
                String email = extractEmail(trimmed);
                emailToAccountData.putIfAbsent(email, trimmed);
            }
        }

        if (emailToAccountData.isEmpty()) {
            return 0;
        }

        // 2. CHỈ QUERY ACCOUNT DATA STRING - không load toàn bộ entity (tối ưu memory + speed)
        Set<String> existingEmails = productItemRepository
                .findAccountDataByProductId(productItemCreateDTO.getProductId())
                .stream()
                .map(this::extractEmail)
                .collect(Collectors.toSet());

        // 3. Filter new items - dùng Set operation
        Set<String> inputEmails = new HashSet<>(emailToAccountData.keySet());
        inputEmails.removeAll(existingEmails);
        
        List<String> newAccountDataList = inputEmails.stream()
                .map(emailToAccountData::get)
                .collect(Collectors.toList());

        if (newAccountDataList.isEmpty()) {
            return nonEmptyInputCount; // All duplicates
        }

        // 4. Prepare batch insert parameters
        Long productId = product.getId();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        ExpirationType expirationType = resolveExpirationType(productItemCreateDTO);
        Timestamp expiresAt = expirationType != ExpirationType.NONE 
                ? Timestamp.valueOf(LocalDateTime.now().plusHours(expirationType.getHours()))
                : null;
        String expirationTypeName = expirationType.name();

        // 5. Batch insert với BATCH SIZE LỚN HƠN (2000 thay vì 500)
        String sql = "INSERT INTO product_items (product_id, account_data, sold, expired, warehouse_type, expiration_type, expires_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, newAccountDataList, 2000, (ps, accountData) -> {
            ps.setLong(1, productId);
            ps.setString(2, accountData);
            ps.setBoolean(3, false);
            ps.setBoolean(4, false);
            ps.setString(5, WarehouseType.PRIMARY.name());
            ps.setString(6, expirationTypeName);
            ps.setTimestamp(7, expiresAt);
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
        });

        log.info("Batch inserted {} product items for product {} (expirationType: {})",
                newAccountDataList.size(), productId, expirationType);

        // 6. Async notifications
        productQuantityNotifier.publishAfterCommit(productItemCreateDTO.getProductId());
        
        // 7. Async warehouse check - không block response
        final Long finalProductId = productId;
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                warehouseService.checkAndTransferStock(finalProductId);
            } catch (Exception e) {
                log.warn("Async warehouse check failed for product {}: {}", finalProductId, e.getMessage());
            }
        });

        return nonEmptyInputCount - newAccountDataList.size();
    }

    /**
     * Resolve ExpirationType from DTO
     */
    private ExpirationType resolveExpirationType(ProductItemCreateDTO dto) {
        if (StringUtils.hasText(dto.getExpirationType())) {
            try {
                return ExpirationType.valueOf(dto.getExpirationType());
            } catch (IllegalArgumentException e) {
                return ExpirationType.NONE;
            }
        }
        Integer expirationHours = dto.getExpirationHours();
        return expirationHours != null ? ExpirationType.fromHours(expirationHours) : ExpirationType.NONE;
    }

    @Override
    public Page<ProductItemResponseDTO> searchProductItems(ProductItemFilterDTO productItemFilterDTO) {
        Sort sort = Utils.generatedSort(productItemFilterDTO.getSort());
        Pageable pageable = PageRequest.of(productItemFilterDTO.getPage(), productItemFilterDTO.getLimit(), sort);

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

            // 3. Filter theo accountData
            if (StringUtils.hasText(req.getAccountData())) {
                predicates.add(cb.like(cb.lower(root.get("accountData")),
                        "%" + req.getAccountData().trim().toLowerCase() + "%"));
            }

            // 4. Filter theo loại thời gian hết hạn (expirationType)
            if (StringUtils.hasText(req.getExpirationType())) {
                try {
                    ExpirationType type = ExpirationType.valueOf(req.getExpirationType());
                    predicates.add(cb.equal(root.get("expirationType"), type));
                } catch (IllegalArgumentException ignored) {
                    // Invalid type, ignore
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Lấy items MỚI NHẤT từ kho PHỤ (SECONDARY) để bán
     * Chỉ bán từ kho phụ, không bán từ kho chính
     * Tự động loại bỏ items đã hết hạn trước khi trả về
     * 
     * FOR UPDATE SKIP LOCKED: đảm bảo nhiều người mua cùng lúc không lấy trùng items
     */
    @Override
    public List<ProductItemEntity> getNewestUnsoldItems(Long productId, int quantity) {
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1. Đánh dấu các items đã hết hạn trước khi lấy (dựa trên expiresAt của từng item)
        markExpiredItems(productId);

        // 2. Lấy từ kho SECONDARY - MỚI NHẤT trước (đã loại bỏ expired items trong query)
        List<ProductItemEntity> items =
                productItemRepository.findNewestUnsoldSecondaryItems(productId, quantity);

        if (items.size() < quantity) {
            // Có thể kho phụ không đủ, thử trigger chuyển kho rồi lấy lại
            warehouseService.checkAndTransferStock(productId);

            // Đánh dấu expired lần nữa cho items mới chuyển sang
            markExpiredItems(productId);

            items = productItemRepository.findNewestUnsoldSecondaryItems(productId, quantity);

            if (items.size() < quantity) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
            }
        }

        // 3. Double-check: lọc ra các items còn hạn (phòng trường hợp race condition)
        List<ProductItemEntity> validItems = new ArrayList<>();
        List<Long> expiredIds = new ArrayList<>();

        for (ProductItemEntity item : items) {
            if (item.isExpired()) { // Dùng isExpired() không tham số - check dựa trên expiresAt
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
    public int importItems(Long productId, MultipartFile file, ExpirationType expirationType) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        List<String> accountDataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    accountDataList.add(trimmed);
                }
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Lỗi đọc file");
        }

        if (accountDataList.isEmpty()) {
            return 0;
        }

        // 1. Deduplicate input by email - optimized
        Map<String, String> emailToAccountData = new LinkedHashMap<>();
        for (String line : accountDataList) {
            String email = extractEmail(line);
            emailToAccountData.putIfAbsent(email, line);
        }

        // 2. CHỈ QUERY ACCOUNT DATA STRING - không load toàn bộ entity
        Set<String> existingEmails = productItemRepository
                .findAccountDataByProductId(productId)
                .stream()
                .map(this::extractEmail)
                .collect(Collectors.toSet());

        // 3. Filter new items - dùng Set operation
        Set<String> inputEmails = new HashSet<>(emailToAccountData.keySet());
        inputEmails.removeAll(existingEmails);
        
        List<String> newAccountDataList = inputEmails.stream()
                .map(emailToAccountData::get)
                .collect(Collectors.toList());

        if (newAccountDataList.isEmpty()) {
            return 0;
        }

        // 4. JDBC Batch Insert với BATCH SIZE LỚN
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp expiresAt = expirationType != ExpirationType.NONE
                ? Timestamp.valueOf(LocalDateTime.now().plusHours(expirationType.getHours()))
                : null;
        String expirationTypeName = expirationType.name();

        String sql = "INSERT INTO product_items (product_id, account_data, sold, expired, warehouse_type, expiration_type, expires_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, newAccountDataList, 2000, (ps, accountData) -> {
            ps.setLong(1, productId);
            ps.setString(2, accountData);
            ps.setBoolean(3, false);
            ps.setBoolean(4, false);
            ps.setString(5, WarehouseType.PRIMARY.name());
            ps.setString(6, expirationTypeName);
            ps.setTimestamp(7, expiresAt);
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
        });

        log.info("Imported {} items for product {} with expirationType {}",
                newAccountDataList.size(), productId, expirationType);

        productQuantityNotifier.publishAfterCommit(productId);
        
        // Async warehouse check
        final Long finalProductId = productId;
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                warehouseService.checkAndTransferStock(finalProductId);
            } catch (Exception e) {
                log.warn("Async warehouse check failed for product {}: {}", finalProductId, e.getMessage());
            }
        });

        return newAccountDataList.size();
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

        // Get expiration type info
        ExpirationType expType = e.getExpirationType() != null ? e.getExpirationType() : ExpirationType.NONE;

        return ProductItemResponseDTO.builder()
                .id(e.getId())
                .productId(e.getProduct().getId())
                .accountData(e.getAccountData())
                .sold(e.getSold())
                .warehouseType(e.getWarehouseType() != null ? e.getWarehouseType().name() : null)
                .buyerId(e.getBuyerId())
                .buyerName(buyerEmail)
                .soldAt(e.getSoldAt() != null ? e.getSoldAt().toString() : null)
                // Expiration info (per item)
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null)
                .expirationType(expType.name())
                .expirationLabel(expType.getLabel())
                .expiresAt(e.getExpiresAt() != null ? e.getExpiresAt().toString() : null)
                .expired(e.getExpired())
                .expiredAt(e.getExpiredAt() != null ? e.getExpiredAt().toString() : null)
                .build();
    }

    // ============ BULK OPERATIONS ============

    /**
     * Xóa items theo danh sách email (chỉ cần email, không cần full accountData)
     * Input: mỗi dòng là 1 email hoặc accountData (sẽ extract email từ đó)
     */
    @Override
    @Transactional
    public int deleteByAccountData(Long productId, String accountDataList) {
        if (!org.springframework.util.StringUtils.hasText(accountDataList)) {
            return 0;
        }

        // Parse input - extract email từ mỗi dòng
        String[] lines = accountDataList.split("\\r?\\n");
        Set<String> inputEmails = new HashSet<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // Extract email từ input (có thể là email hoặc full accountData)
                String email = extractEmail(trimmed);
                inputEmails.add(email);
            }
        }

        if (inputEmails.isEmpty()) {
            return 0;
        }

        // Lấy tất cả items của product này (chưa bán)
        List<ProductItemEntity> allItems = productItemRepository.findByProductId(productId);
        
        // Tìm items có email match với input
        List<Long> idsToDelete = new ArrayList<>();
        for (ProductItemEntity item : allItems) {
            if (item.getSold()) continue; // Skip items đã bán
            
            String itemEmail = extractEmail(item.getAccountData());
            if (inputEmails.contains(itemEmail)) {
                idsToDelete.add(item.getId());
            }
        }

        if (idsToDelete.isEmpty()) {
            return 0;
        }

        // Delete by IDs
        productItemRepository.deleteAllById(idsToDelete);
        
        log.info("Product {}: Deleted {} items by email match", productId, idsToDelete.size());
        productQuantityNotifier.publishAfterCommit(productId);
        
        return idsToDelete.size();
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

    /**
     * Extract email from accountData string
     * Format: email|password|... → returns email
     * If no | separator, returns entire string
     */
    private String extractEmail(String accountData) {
        if (accountData == null || accountData.isEmpty()) {
            return "";
        }
        int separatorIndex = accountData.indexOf('|');
        if (separatorIndex > 0) {
            return accountData.substring(0, separatorIndex).trim().toLowerCase();
        }
        return accountData.trim().toLowerCase();
    }
}

