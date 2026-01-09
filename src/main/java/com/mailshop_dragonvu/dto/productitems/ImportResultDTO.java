package com.mailshop_dragonvu.dto.productitems;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa kết quả import ProductItems
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {
    
    /**
     * Số lượng tài khoản import thành công
     */
    private int importedCount;
    
    /**
     * Số lượng tài khoản bị trùng (skip)
     */
    private int duplicateCount;
    
    /**
     * Tổng số dòng trong input
     */
    private int totalInput;
    
    /**
     * Message tổng hợp
     */
    public String getMessage() {
        if (duplicateCount > 0) {
            return String.format("Import thành công %d tài khoản. Có %d tài khoản bị trùng.", 
                    importedCount, duplicateCount);
        }
        return String.format("Import thành công %d tài khoản.", importedCount);
    }
}
