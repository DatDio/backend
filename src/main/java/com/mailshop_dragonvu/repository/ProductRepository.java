package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    List<ProductEntity> findAllByStatus(ActiveStatusEnum status);

//
//    @Query("SELECT p FROM ProductEntity p WHERE " +
//           "p.isDeleted = false AND " +
//           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
//           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
//           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
//           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
//           "(:isActive IS NULL OR p.isActive = :isActive)")
//    Page<ProductEntity> searchProducts(
//            @Param("name") String name,
//            @Param("categoryId") Long categoryId,
//            @Param("minPrice") BigDecimal minPrice,
//            @Param("maxPrice") BigDecimal maxPrice,
//            @Param("isActive") Boolean isActive,
//            Pageable pageable);

}
