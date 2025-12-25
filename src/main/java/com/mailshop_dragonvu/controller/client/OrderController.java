package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.orders.*;
import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.OrderService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_PATH.ORDERS)
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClientOrderCreateResponseDTO> createOrder(
            @Valid @RequestBody OrderCreateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success("Mua hàng thành công",
                orderService.createOrder(request, userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponseDTO> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success(orderService.getOrderById(id, userPrincipal.getId()));
    }

    @GetMapping("/my-orders")
    public ApiResponse<Page<OrderResponseDTO>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            OrderFilterDTO filterDTO) {

        filterDTO.setUserId(userPrincipal.getId()); // ÉP USER ID TỪ TOKEN

        return ApiResponse.success(orderService.search(filterDTO));
    }


}
