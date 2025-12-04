package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductItemService {
    int batchCreateProductItems(ProductItemCreateDTO productItemCreateDTO);

    Page<ProductItemResponseDTO> searchProductItems(ProductItemFilterDTO productItemFilterDTO);

    List<ProductItemEntity> getRandomUnsoldItems(Long productId, int quantity);

    void importItems(Long productId, MultipartFile file);

    void deleteItem(Long id);

}
