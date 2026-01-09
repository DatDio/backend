package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.productitems.ProductItemCreateDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemFilterDTO;
import com.mailshop_dragonvu.dto.productitems.ProductItemResponseDTO;
import com.mailshop_dragonvu.enums.ExpirationType;
import com.mailshop_dragonvu.service.ProductItemService;
import com.mailshop_dragonvu.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    // IMPORT TXT FILE với ExpirationType
    @PostMapping("/import/{productId}")
    public ApiResponse<String> importItems(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expirationType", defaultValue = "NONE") ExpirationType expirationType
    ) {
        var result = productItemService.importItems(productId, file, expirationType);
        return ApiResponse.success(result.getMessage());
    }

    // DELETE ITEM
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productItemService.deleteItem(id);
        return ApiResponse.success("Deleted");
    }

    // ============ BULK OPERATIONS ============

    /**
     * Xóa nhiều items theo danh sách account data
     * Input: productId, accountDataList (mỗi dòng một account)
     */
    @PostMapping("/bulk-delete/{productId}")
    public ApiResponse<Map<String, Object>> bulkDelete(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request
    ) {
        String accountDataList = request.get("accountDataList");
        int deleted = productItemService.deleteByAccountData(productId, accountDataList);
        return ApiResponse.success(Map.of(
                "deleted", deleted,
                "message", "Đã xóa " + deleted + " tài khoản"
        ));
    }

    /**
     * Lấy danh sách items đã hết hạn (để export)
     */
    @GetMapping("/expired/{productId}")
    public ApiResponse<List<ProductItemResponseDTO>> getExpiredItems(@PathVariable Long productId) {
        return ApiResponse.success(productItemService.getExpiredItems(productId));
    }

    /**
     * Xóa tất cả items đã hết hạn
     */
    @DeleteMapping("/expired/{productId}")
    public ApiResponse<Map<String, Object>> deleteExpiredItems(@PathVariable Long productId) {
        int deleted = productItemService.deleteExpiredItems(productId);
        return ApiResponse.success(Map.of(
                "deleted", deleted,
                "message", "Đã xóa " + deleted + " tài khoản hết hạn"
        ));
    }

}

