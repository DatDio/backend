package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.transactions.TransactionFilterDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionUpdateStatusDTO;
import com.mailshop_dragonvu.service.WalletService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController("adminTransactionController")
@RequestMapping("/admin/" + Constants.API_PATH.TRANSACTIONS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Transaction Management", description = "Admin APIs for managing transactions")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class TransactionController {

    private final WalletService walletService;

    @GetMapping
    public ApiResponse<Page<TransactionResponseDTO>> searchTransactions(TransactionFilterDTO filter) {
        log.info("Admin searching transactions with filter: {}", filter);
        return ApiResponse.success(walletService.searchAllTransactions(filter));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<TransactionResponseDTO> updateTransactionStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateStatusDTO request) {
        log.info("Admin updating transaction {} status to {}", id, request.getStatus());
        TransactionResponseDTO result = walletService.updateTransactionStatus(id, request.getStatus(), request.getReason());
        return ApiResponse.success("Cập nhật trạng thái thành công", result);
    }
}
