package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.hotmail.HotmailGetCodeRequestDTO;
import com.mailshop_dragonvu.dto.hotmail.HotmailGetCodeResponseDTO;

import java.util.List;

/**
 * Service interface for Hotmail operations
 */
public interface HotmailService {

    /**
     * Get verification code from Hotmail inbox
     * 
     * @param request contains email credentials and filter options
     * @return list of emails with extracted codes
     */
    List<HotmailGetCodeResponseDTO> getCode(HotmailGetCodeRequestDTO request);
}
