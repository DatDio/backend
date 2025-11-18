package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.orders.OrderCreateRequest;
import com.mailshop_dragonvu.dto.orders.OrderUpdateRequest;
import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.orders.OrderResponse;
import com.mailshop_dragonvu.enums.OrderStatus;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public ApiResponse<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success("Order created successfully", 
                orderService.createOrder(request, userPrincipal.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order by ID")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success("Order updated successfully", 
                orderService.updateOrder(id, request, userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ApiResponse<OrderResponse> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success(orderService.getOrderById(id, userPrincipal.getId()));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ApiResponse<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success(orderService.getOrderByNumber(orderNumber, userPrincipal.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders with pagination (Admin only)")
    public ApiResponse<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        return ApiResponse.success(orderService.getAllOrders(pageable));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current user's orders")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        return ApiResponse.success(orderService.getOrdersByUser(userPrincipal.getId(), pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status (Admin only)")
    public ApiResponse<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        return ApiResponse.success(orderService.getOrdersByStatus(status, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (Admin only)")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ApiResponse.success("Order status updated successfully", 
                orderService.updateOrderStatus(id, status));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm order (Admin only)")
    public ApiResponse<OrderResponse> confirmOrder(@PathVariable Long id) {
        return ApiResponse.success("Order confirmed successfully", 
                orderService.confirmOrder(id));
    }


    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success("Order cancelled successfully", 
                orderService.cancelOrder(id, reason, userPrincipal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete order (Admin only)")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success("Order deleted successfully");
    }

}
