package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_PATH.ORDERS)
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public ApiResponse<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderCreateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success("Order created successfully", 
                orderService.createOrder(request, userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ApiResponse<OrderResponseDTO> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success(orderService.getOrderById(id, userPrincipal.getId()));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current user's orders")
    public ApiResponse<Page<OrderResponseDTO>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        return ApiResponse.success(orderService.getOrdersByUser(userPrincipal.getId(), pageable));
    }

}
