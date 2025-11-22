package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import com.mailshop_dragonvu.repository.ProductRepository;
import com.mailshop_dragonvu.service.ProductItemService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductItemServiceImpl implements ProductItemService {

    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    @Override
    public void batchCreateProductItems(ProductItemCreateDTO productItemCreateDTO) {
        ProductEntity product = productRepository.findById(productItemCreateDTO.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if(!StringUtils.hasText(productItemCreateDTO.getAccountData())){
            throw new BusinessException("Nội dung rỗng!");
        }

        String[] lines = productItemCreateDTO.getAccountData().split("\\r?\\n");
        List<ProductItemEntity> items = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Tạo entity
            ProductItemEntity item = ProductItemEntity.builder()
                    .product(product)
                    .accountData(line)
                    .sold(false)
                    .build();

            items.add(item);
        }

        productItemRepository.saveAll(items);
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<ProductItemEntity> getRandomUnsoldItems(Long productId, int quantity) {
        List<ProductItemEntity> items =
                productItemRepository.findRandomUnsoldItems(productId, quantity);

        if (items.size() < quantity) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }

        return items;
    }

    public void markSold(Long itemId, Long buyerId, Long orderId) {
        productItemRepository.markAsSold(itemId, buyerId, orderId);
    }

    @Override
    public void deleteItem(Long id) {
        productItemRepository.deleteById(id);
    }

    @Override
    public void importItems(Long productId, MultipartFile file) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                // Example Format: mail|pass|recovery
                ProductItemEntity item = ProductItemEntity.builder()
                        .product(product)
                        .accountData(trimmed)
                        .sold(false)
                        .build();

                productItemRepository.save(item);
                count++;
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Lỗi đọc file");
        }
    }

    // Convert ENTITY → DTO
    private ProductItemResponseDTO toDTO(ProductItemEntity e) {
        return ProductItemResponseDTO.builder()
                .id(e.getId())
                .productId(e.getProduct().getId())
                .accountData(e.getAccountData())
                .sold(e.getSold())
                .buyerId(e.getBuyerId())
                .orderId(e.getOrderId())
                .soldAt(e.getSoldAt() != null ? e.getSoldAt().toString() : null)
                .build();
    }
}
