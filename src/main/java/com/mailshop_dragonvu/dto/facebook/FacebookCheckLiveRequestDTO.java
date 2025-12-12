package com.mailshop_dragonvu.dto.facebook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Facebook check live
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookCheckLiveRequestDTO {
    private String uidData;
}
