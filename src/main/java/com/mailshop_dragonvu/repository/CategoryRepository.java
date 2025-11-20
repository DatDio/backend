package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.CategoryEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByName(String name);

    @Query("SELECT c FROM CategoryEntity c WHERE c.status = :status")
    Page<CategoryEntity> findByStatus(@Param("status") ActiveStatusEnum status, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<CategoryEntity> searchCategories(
            @Param("name") String name,
            @Param("status") ActiveStatusEnum status,
            Pageable pageable);

    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.status = :status")
    Integer countByStatus(@Param("status") ActiveStatusEnum status);

}
