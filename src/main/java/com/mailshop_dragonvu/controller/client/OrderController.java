package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.orders.*;
import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.MessageService;
import com.mailshop_dragonvu.service.OrderService;
import com.mailshop_dragonvu.utils.Constants;
import com.mailshop_dragonvu.utils.MessageKeys;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_PATH.ORDERS)
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;
    private final MessageService messageService;

    /**
     * Mua mail - hỗ trợ cả header và query parameter apikey
     * VD: GET /api/v1/orders/buy?productId=1&quantity=10&apikey=msk_xxx
     */
    @GetMapping("/buy")
    public ApiResponse<ClientOrderCreateResponseDTO> createOrder(
            @Valid OrderCreateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success(messageService.getMessage(MessageKeys.Order.PURCHASE),
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
