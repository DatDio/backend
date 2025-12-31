package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.products.ProductCreateDTO;
import com.mailshop_dragonvu.dto.products.ProductFilterDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductUpdateDTO;
import com.mailshop_dragonvu.entity.CategoryEntity;
import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.repository.CategoryRepository;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import com.mailshop_dragonvu.repository.ProductRepository;
import com.mailshop_dragonvu.service.FileUploadService;
import com.mailshop_dragonvu.service.ProductService;
import com.mailshop_dragonvu.utils.Utils;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;

    private static final String IMAGE_FOLDER = "products";

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllActiveProducts() {
        return productRepository.findAllByStatus(ActiveStatusEnum.ACTIVE)
                .stream()
                .map(this::toProductResponseClient)
                .toList();
    }

    @Override
    public ProductResponseDTO createProduct(ProductCreateDTO request) {

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .liveTime(request.getLiveTime())
                .country(request.getCountry())
                .price(request.getPrice())
                .category(category)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .minSecondaryStock(request.getMinSecondaryStock() != null ? request.getMinSecondaryStock() : 500)
                .maxSecondaryStock(request.getMaxSecondaryStock() != null ? request.getMaxSecondaryStock() : 1000)
                .expirationHours(request.getExpirationHours() != null ? request.getExpirationHours() : 0)
                .status(ActiveStatusEnum.ACTIVE)
                .build();

        // Handle image upload
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileUploadService.uploadImage(request.getImage(), IMAGE_FOLDER);
            product.setImageUrl(imageUrl);
        }

        ProductEntity saved = productRepository.save(product);

        return toProductResponse(saved);
    }

    // ================== UPDATE PRODUCT ==================

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO request) {
        log.info("Updating product {}", id);

        ProductEntity product = findProductOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            product.setCategory(category);
        }
        product.setLiveTime(request.getLiveTime());
        product.setCountry(request.getCountry());

        if (request.getSortOrder() != null) {
            product.setSortOrder(request.getSortOrder());
        }

        // Cập nhật cấu hình kho phụ
        if (request.getMinSecondaryStock() != null) {
            product.setMinSecondaryStock(request.getMinSecondaryStock());
        }
        if (request.getMaxSecondaryStock() != null) {
            product.setMaxSecondaryStock(request.getMaxSecondaryStock());
        }

        // Cập nhật thời gian hết hạn
        if (request.getExpirationHours() != null) {
            product.setExpirationHours(request.getExpirationHours());
        }

        if (request.getStatus() != null)
            product.setStatus(ActiveStatusEnum.fromKey((request.getStatus())));
        // Delete old image if exists
        if (product.getImageUrl() != null) {
            fileUploadService.deleteFile(product.getImageUrl());
            product.setImageUrl(null);
        }

        // Handle image upload
        if (request.getImage() != null && !request.getImage().isEmpty()) {

            String imageUrl = fileUploadService.uploadImage(request.getImage(), IMAGE_FOLDER);
            product.setImageUrl(imageUrl);
        }

        ProductEntity updated = productRepository.save(product);

        return toProductResponse(updated);
    }

    // ================== GET PRODUCT BY ID ==================

    @Override
    public ProductResponseDTO getProductById(Long id) {
        ProductEntity product = findProductOrThrow(id);
        return toProductResponse(product);
    }


    // ================== DELETE PRODUCT ==================

    @Override
    public void deleteProduct(Long id) {
        ProductEntity product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    // ================== ACTIVATE / DEACTIVATE ==================

    @Override
    public void activateProduct(Long id) {
        ProductEntity product = findProductOrThrow(id);
        product.setStatus(ActiveStatusEnum.ACTIVE);
        productRepository.save(product);
    }

    @Override
    public void deactivateProduct(Long id) {
        ProductEntity product = findProductOrThrow(id);
        product.setStatus(ActiveStatusEnum.INACTIVE);
        productRepository.save(product);
    }

    // ================== SEARCH PRODUCTS ==================

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(ProductFilterDTO request) {
        Sort sort = Utils.generatedSort(request.getSort());
        Pageable pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);

        Specification<ProductEntity> spec = getSearchSpecification(request);

        Page<ProductEntity> page = productRepository.findAll(spec, pageable);

        return page.map(this::toProductResponse);
    }

    private Specification<ProductEntity> getSearchSpecification(ProductFilterDTO req) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ---- NAME (LIKE) ----
            if (req.getName() != null && !req.getName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + req.getName().trim().toLowerCase() + "%"
                        )
                );
            }

            // ---- CATEGORY ----
            if (req.getCategoryId() != null) {
                predicates.add(
                        cb.equal(root.get("category").get("id"), req.getCategoryId())
                );
            }

            // ---- MIN PRICE ----
            if (req.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("price"), req.getMinPrice())
                );
            }

            // ---- MAX PRICE ----
            if (req.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("price"), req.getMaxPrice())
                );
            }

            // ---- STATUS ----
            if (!ObjectUtils.isEmpty(req.getStatus()) && !req.getStatus().isBlank()) {
                Set<ActiveStatusEnum> statusEnums = Arrays.stream(req.getStatus().split(","))
                        .map(s -> ActiveStatusEnum.fromKey(Integer.valueOf(s.trim())))
                        .collect(Collectors.toSet());
                predicates.add(root.get("status").in(statusEnums));
            }

            // ---- MIN STOCK (số lượng còn lại >= minStock) ----
            // Ở đây phải subquery count ProductItem chưa bán
            if (req.getMinStock() != null) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<ProductItemEntity> itemRoot = sub.from(ProductItemEntity.class);

                sub.select(cb.count(itemRoot));
                sub.where(
                        cb.equal(itemRoot.get("product").get("id"), root.get("id")),
                        cb.isFalse(itemRoot.get("sold"))
                );

                predicates.add(cb.greaterThanOrEqualTo(sub, req.getMinStock().longValue()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    // ================== HELPER ==================

    private ProductEntity findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));
    }

    /**
     * Convert product + tính số lượng còn lại từ ProductItem
     * - quantity: Số lượng kho PHỤ (hiển thị cho khách hàng)
     * - primaryQuantity: Số lượng kho CHÍNH (hiển thị cho admin)
     */
    private ProductResponseDTO toProductResponse(ProductEntity product) {
        // Số lượng kho PHỤ - hiển thị cho khách hàng
        long secondaryQuantity = productItemRepository.countSecondaryItems(product.getId());
        // Số lượng kho CHÍNH - chỉ hiển thị cho admin
        long primaryQuantity = productItemRepository.countPrimaryItems(product.getId());

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .liveTime(product.getLiveTime())
                .country(product.getCountry())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus().getKey())
                .quantity(secondaryQuantity)           // Kho phụ - hiển thị cho khách
                .primaryQuantity(primaryQuantity)       // Kho chính - hiển thị cho admin
                .minSecondaryStock(product.getMinSecondaryStock())
                .maxSecondaryStock(product.getMaxSecondaryStock())
                .expirationHours(product.getExpirationHours())
                .sortOrder(product.getSortOrder())
                .build();
    }

    private ProductResponseDTO toProductResponseClient(ProductEntity product) {
        // Số lượng kho PHỤ - hiển thị cho khách hàng
        long secondaryQuantity = productItemRepository.countSecondaryItems(product.getId());

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .liveTime(product.getLiveTime())
                .country(product.getCountry())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus().getKey())
                .quantity(secondaryQuantity)           // Kho phụ - hiển thị cho khách
                .minSecondaryStock(product.getMinSecondaryStock())
                .maxSecondaryStock(product.getMaxSecondaryStock())
                .expirationHours(product.getExpirationHours())
                .sortOrder(product.getSortOrder())
                .build();
    }
}

