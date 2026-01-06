package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.service.CassoProvider;
import com.mailshop_dragonvu.service.CassoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Casso Service Implementation
 * Generates VietQR codes and handles webhook verification for Casso
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CassoServiceImpl implements CassoService {

    private final CassoProvider cassoProvider;

    // VietQR.io URL format for generating QR codes - using 'print' template for full info display
    private static final String VIETQR_URL_TEMPLATE = 
        "https://img.vietqr.io/image/%s-%s-print.png?amount=%d&addInfo=%s&accountName=%s";

    // Bank code to BIN mapping (NAPAS standard)
    private static final Map<String, String> BANK_BIN_MAP = new HashMap<>();
    private static final Map<String, String> BANK_NAME_MAP = new HashMap<>();

    static {
        // Popular Vietnamese banks
        BANK_BIN_MAP.put("ACB", "970416");
        BANK_BIN_MAP.put("VCB", "970436");
        BANK_BIN_MAP.put("TCB", "970407");
        BANK_BIN_MAP.put("MB", "970422");
        BANK_BIN_MAP.put("TPB", "970423");
        BANK_BIN_MAP.put("VPB", "970432");
        BANK_BIN_MAP.put("BIDV", "970418");
        BANK_BIN_MAP.put("VTB", "970415");
        BANK_BIN_MAP.put("STB", "970403");
        BANK_BIN_MAP.put("MSB", "970426");
        BANK_BIN_MAP.put("HDB", "970437");
        BANK_BIN_MAP.put("OCB", "970448");
        BANK_BIN_MAP.put("SHB", "970443");
        BANK_BIN_MAP.put("EIB", "970431");
        BANK_BIN_MAP.put("NAB", "970428");
        BANK_BIN_MAP.put("ABB", "970425");
        BANK_BIN_MAP.put("BAB", "970409");
        BANK_BIN_MAP.put("SCB", "970429");
        BANK_BIN_MAP.put("SEAB", "970440");
        BANK_BIN_MAP.put("LPB", "970449");
        BANK_BIN_MAP.put("KLB", "970452");
        BANK_BIN_MAP.put("CAKE", "546034");
        BANK_BIN_MAP.put("UBANK", "546035");
        
        BANK_NAME_MAP.put("ACB", "Ngân hàng TMCP Á Châu");
        BANK_NAME_MAP.put("VCB", "Ngân hàng TMCP Ngoại Thương Việt Nam");
        BANK_NAME_MAP.put("TCB", "Ngân hàng TMCP Kỹ Thương Việt Nam");
        BANK_NAME_MAP.put("MB", "Ngân hàng TMCP Quân Đội");
        BANK_NAME_MAP.put("TPB", "Ngân hàng TMCP Tiên Phong");
        BANK_NAME_MAP.put("VPB", "Ngân hàng TMCP Việt Nam Thịnh Vượng");
        BANK_NAME_MAP.put("BIDV", "Ngân hàng TMCP Đầu Tư và Phát Triển Việt Nam");
        BANK_NAME_MAP.put("VTB", "Ngân hàng TMCP Công Thương Việt Nam");
        BANK_NAME_MAP.put("STB", "Ngân hàng TMCP Sài Gòn Thương Tín");
        BANK_NAME_MAP.put("MSB", "Ngân hàng TMCP Hàng Hải Việt Nam");
        BANK_NAME_MAP.put("HDB", "Ngân hàng TMCP Phát Triển TP.HCM");
        BANK_NAME_MAP.put("OCB", "Ngân hàng TMCP Phương Đông");
        BANK_NAME_MAP.put("SHB", "Ngân hàng TMCP Sài Gòn - Hà Nội");
        BANK_NAME_MAP.put("EIB", "Ngân hàng TMCP Xuất Nhập Khẩu Việt Nam");
    }

    @Override
    public String generateQRCodeUrl(Long amount, Long transactionCode) {
        cassoProvider.validateConfiguration();

        String bankCode = cassoProvider.getBankCode();
        String bankBin = BANK_BIN_MAP.getOrDefault(bankCode.toUpperCase(), bankCode);
        String accountNo = cassoProvider.getBankAccount();
        String accountName = cassoProvider.getAccountName();
        
        // Generate transfer content with transaction code (no prefix)
        String transferContent = String.valueOf(transactionCode);
        
        try {
            String encodedContent = URLEncoder.encode(transferContent, StandardCharsets.UTF_8.toString());
            String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
            
            String qrUrl = String.format(VIETQR_URL_TEMPLATE,
                bankBin,
                accountNo,
                amount,
                encodedContent,
                encodedName
            );
            
            log.info("Generated VietQR URL for transaction {}: amount={}", transactionCode, amount);
            return qrUrl;
            
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode QR URL parameters", e);
            // Fallback without encoding
            return String.format(VIETQR_URL_TEMPLATE,
                bankBin,
                accountNo,
                amount,
                transferContent,
                accountName
            );
        }
    }

    @Override
    public boolean verifyWebhook(String secureToken) {
        String configuredToken = cassoProvider.getSecureToken();
        
        if (Strings.isBlank(configuredToken)) {
            log.warn("Casso secure token not configured, skipping verification");
            return true; // Allow if not configured (for testing)
        }

        if (Strings.isBlank(secureToken)) {
            log.warn("Missing secure-token header in Casso webhook request");
            return false;
        }

        boolean isValid = configuredToken.equals(secureToken);
        if (!isValid) {
            log.warn("Invalid Casso webhook secure token");
        }
        return isValid;
    }

    @Override
    public String getBankName(String bankCode) {
        return BANK_NAME_MAP.getOrDefault(bankCode.toUpperCase(), bankCode);
    }
}
