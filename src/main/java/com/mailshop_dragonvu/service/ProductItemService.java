package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.productitems.ImportResultDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.enums.ExpirationType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductItemService {
    int batchCreateProductItems(ProductItemCreateDTO productItemCreateDTO);

    Page<ProductItemResponseDTO> searchProductItems(ProductItemFilterDTO productItemFilterDTO);

    List<ProductItemEntity> getNewestUnsoldItems(Long productId, int quantity);

    ImportResultDTO importItems(Long productId, MultipartFile file, ExpirationType expirationType);

    void deleteItem(Long id);

    // ============ BULK OPERATIONS ============
    
    /**
     * Xóa nhiều items theo danh sách account data
     * @return số items đã xóa
     */
    int deleteByAccountData(Long productId, String accountDataList);
    
    /**
     * Lấy danh sách items đã hết hạn (để export)
     */
    List<ProductItemResponseDTO> getExpiredItems(Long productId);
    
    /**
     * Xóa tất cả items đã hết hạn
     * @return số items đã xóa
     */
    int deleteExpiredItems(Long productId);
}

