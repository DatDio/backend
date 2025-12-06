package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.service.ProductItemService;
import com.mailshop_dragonvu.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/" + Constants.API_PATH.PRODUCTITEMS)
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class ProductItemController {

    private final ProductItemService productItemService;

    // GET ITEMS OF PRODUCT
    @GetMapping("/search")
    public ApiResponse<Page<ProductItemResponseDTO>> getItems(ProductItemFilterDTO productItemFilterDTO) {
        return ApiResponse.success(productItemService.searchProductItems(productItemFilterDTO));
    }

    @PostMapping("/create")
    public ApiResponse<String> Create(@RequestBody ProductItemCreateDTO productItemCreateDTO) {
        int duplicateCount = productItemService.batchCreateProductItems(productItemCreateDTO);
        if (duplicateCount > 0) {
            return ApiResponse.success("Thêm thành công. Có " + duplicateCount + " tài khoản bị trùng.");
        }
        return ApiResponse.success("Thêm thành công");
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
