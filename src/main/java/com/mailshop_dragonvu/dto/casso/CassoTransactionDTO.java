package com.mailshop_dragonvu.dto.casso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Casso Transaction DTO
 * Represents transaction data from Casso webhook
 * 
 * Sample data:
 * {
 *   "id": 123456,
 *   "reference": "MA_GIAO_DICH",
 *   "description": "NAPTIEN1234567890",
 *   "amount": 50000,
 *   "runningBalance": 25000000,
 *   "transactionDateTime": "2025-01-01 12:00:00",
 *   "accountNumber": "88888888",
 *   "bankName": "VPBank",
 *   "bankAbbreviation": "VPB",
 *   "counterAccountName": "NGUYEN VAN A",
 *   "counterAccountNumber": "8888888888"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CassoTransactionDTO {
    
    /**
     * Casso internal transaction ID - used for deduplication
     */
    private Long id;
    
    /**
     * Transaction ID (alternative field name from Casso)
     */
    private String tid;
    
    /**
     * Bank reference number
     */
    private String reference;
    
    /**
     * Transfer content/description - CRITICAL for matching
     */
    private String description;
    
    /**
     * Transfer amount (as number)
     */
    private Long amount;
    
    /**
     * Account balance after transaction
     */
    private Long runningBalance;
    
    /**
     * Transaction date time (format: yyyy-MM-dd HH:mm:ss)
     */
    private String transactionDateTime;
    
    /**
     * Receiving bank account number
     */
    private String accountNumber;
    
    /**
     * Receiving bank name
     */
    private String bankName;
    
    /**
     * Bank abbreviation (VPB, ACB, etc.)
     */
    private String bankAbbreviation;
    
    /**
     * Virtual account number (if using)
     */
    private String virtualAccountNumber;
    
    /**
     * Virtual account name
     */
    private String virtualAccountName;
    
    /**
     * Sender name
     */
    private String counterAccountName;
    
    /**
     * Sender account number
     */
    private String counterAccountNumber;
    
    /**
     * Sender bank ID (BIN)
     */
    private String counterAccountBankId;
    
    /**
     * Sender bank name
     */
    private String counterAccountBankName;
}
