package com.mailshop_dragonvu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> orderItems;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "Shipping city cannot exceed 100 characters")
    private String shippingCity;

    @NotBlank(message = "Shipping state is required")
    @Size(max = 100, message = "Shipping state cannot exceed 100 characters")
    private String shippingState;

    @NotBlank(message = "Shipping postal code is required")
    @Size(max = 20, message = "Shipping postal code cannot exceed 20 characters")
    private String shippingPostalCode;

    @NotBlank(message = "Shipping country is required")
    @Size(max = 100, message = "Shipping country cannot exceed 100 characters")
    private String shippingCountry;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

}
