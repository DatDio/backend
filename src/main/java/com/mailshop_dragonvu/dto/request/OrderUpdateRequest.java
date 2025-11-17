package com.mailshop_dragonvu.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {

    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    @Size(max = 100, message = "Shipping city cannot exceed 100 characters")
    private String shippingCity;

    @Size(max = 100, message = "Shipping state cannot exceed 100 characters")
    private String shippingState;

    @Size(max = 20, message = "Shipping postal code cannot exceed 20 characters")
    private String shippingPostalCode;

    @Size(max = 100, message = "Shipping country cannot exceed 100 characters")
    private String shippingCountry;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

}
