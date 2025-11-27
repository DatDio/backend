package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.orders.OrderFilterDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.service.OrderService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminOrderController")
@RequestMapping("/admin/" + Constants.API_PATH.ORDERS)
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/search")
    @Operation(summary = "Get all users with pagination")
    public ApiResponse<Page<OrderResponseDTO>> search(OrderFilterDTO orderFilterDTO) {
        return ApiResponse.success(orderService.search(orderFilterDTO));
    }
}
