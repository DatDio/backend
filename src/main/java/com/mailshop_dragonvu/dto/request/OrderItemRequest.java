package com.mailshop_dragonvu.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String productName;

    @Size(max = 100, message = "Product SKU cannot exceed 100 characters")
    private String productSku;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    private String notes;

}
