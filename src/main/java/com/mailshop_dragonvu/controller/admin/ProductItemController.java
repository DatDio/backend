package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/product-items")
@RequiredArgsConstructor
@Slf4j
public class ProductItemController {

    private final ProductItemService productItemService;

    // GET ITEMS OF PRODUCT
    @GetMapping("/search")
    public ApiResponse<Page<ProductItemResponseDTO>> getItems(ProductItemFilterDTO productItemFilterDTO) {
        return ApiResponse.success(productItemService.searchProductItems(productItemFilterDTO));
    }

    // IMPORT TXT FILE
    @PostMapping("/import/{productId}")
    public ResponseEntity<?> importItems(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        productItemService.importItems(productId, file);
        return ResponseEntity.ok("Imported successfully");
    }

    // DELETE ITEM
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productItemService.deleteItem(id);
        return ApiResponse.success("Deleted");
    }

}
