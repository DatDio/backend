package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ProductItemService {
    void batchCreateProductItems(ProductItemCreateDTO productItemCreateDTO);

    Page<ProductItemResponseDTO> searchProductItems(ProductItemFilterDTO productItemFilterDTO);

    ProductItemResponseDTO getRandomUnsoldItem(Long productId);

    void importItems(Long productId, MultipartFile file);

    void deleteItem(Long id);

    void markSold(Long itemId, Long buyerId, Long orderId);
}
