package com.mailshop_dragonvu.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    @Size(max = 100, message = "Billing name cannot exceed 100 characters")
    private String billingName;

    @Email(message = "Billing email should be valid")
    @Size(max = 100, message = "Billing email cannot exceed 100 characters")
    private String billingEmail;

    @Size(max = 500, message = "Billing address cannot exceed 500 characters")
    private String billingAddress;

    @Size(max = 20, message = "Billing phone cannot exceed 20 characters")
    private String billingPhone;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 2000, message = "Terms and conditions cannot exceed 2000 characters")
    private String termsAndConditions;

}
