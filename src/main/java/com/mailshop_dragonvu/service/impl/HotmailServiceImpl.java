package com.mailshop_dragonvu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailshop_dragonvu.dto.hotmail.HotmailGetCodeRequestDTO;
import com.mailshop_dragonvu.dto.hotmail.HotmailGetCodeResponseDTO;
import com.mailshop_dragonvu.service.HotmailService;
import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of HotmailService
 * Supports both Graph API and IMAP methods for reading emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HotmailServiceImpl implements HotmailService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GRAPH_TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    private static final String IMAP_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String GRAPH_MESSAGES_URL = "https://graph.microsoft.com/v1.0/me/messages";
    private static final String IMAP_HOST = "outlook.office365.com";
    private static final int IMAP_PORT = 993;
    private static final String DEFAULT_CLIENT_ID = "9e5f94bc-e8a4-4e73-b8be-63364c29d753";

    // Regex pattern to extract 5-10 digit codes
    private static final Pattern CODE_PATTERN = Pattern.compile("(\\d{5,10})");
    
    // Date formatter for response
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = 
        java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    @Override
    public List<HotmailGetCodeResponseDTO> getCode(HotmailGetCodeRequestDTO request) {
        List<HotmailGetCodeResponseDTO> allResults = new ArrayList<>();
        
        // Split emailData by newlines to support multiple emails
        String[] emailLines = request.getEmailData().trim().split("\\n");
        
        for (String emailLine : emailLines) {
            emailLine = emailLine.trim();
            if (emailLine.isEmpty()) continue;
            
            HotmailGetCodeResponseDTO result = processEmailLine(emailLine, request);
            allResults.add(result);
        }
        
        return allResults;
    }

    /**
     * Process a single email line and get verification code
     */
    private HotmailGetCodeResponseDTO processEmailLine(String emailLine, HotmailGetCodeRequestDTO request) {
        try {
            // Parse email data: email|password|refresh_token|client_id
            String[] parts = emailLine.split("\\|");
            if (parts.length < 3) {
                log.error("Invalid email data format for line: {}", emailLine);
                return HotmailGetCodeResponseDTO.builder()
                        .email(parts.length > 0 ? parts[0] : emailLine)
                        .password(parts.length > 1 ? parts[1] : "")
                        .status(false)
                        .content("Invalid format")
                        .build();
            }

            String emailAddr = parts[0];
            String password = parts[1];
            String refreshToken = parts.length > 2 ? parts[2] : "";
            String clientId = parts.length > 3 && !parts[3].isEmpty() ? parts[3] : DEFAULT_CLIENT_ID;

            // Try to get code using Graph API or IMAP
            List<String> emailTypes = request.getEmailTypes();
            if (emailTypes == null || emailTypes.isEmpty()) {
                emailTypes = List.of("Auto");
            }

            // Try Graph API first (OAuth2)
            if ("Oauth2".equalsIgnoreCase(request.getGetType()) || "Graph API".equalsIgnoreCase(request.getGetType())) {
                TokenResult graphToken = refreshAccessTokenGraph(refreshToken, clientId);
                if (graphToken != null && graphToken.isGraphToken) {
                    log.info("Reading mail using Graph API for: {}", emailAddr);
                    HotmailGetCodeResponseDTO codeResult = readMailByGraphNew(graphToken.accessToken, emailAddr, password, emailTypes);
                    if (codeResult != null) {
                        return codeResult;
                    }
                }

                // Fallback to IMAP with OAuth
                TokenResult imapToken = refreshAccessTokenImap(refreshToken, clientId);
                if (imapToken != null) {
                    log.info("Reading mail using IMAP OAuth for: {}", emailAddr);
                    HotmailGetCodeResponseDTO codeResult = readMailByImapNew(emailAddr, password, imapToken.accessToken, emailTypes);
                    if (codeResult != null) {
                        return codeResult;
                    }
                }
            }

            // Could not get access token or no code found
            log.warn("Could not get access token or code for: {}", emailAddr);
            return HotmailGetCodeResponseDTO.builder()
                    .email(emailAddr)
                    .password(password)
                    .status(false)
                    .content("Could not get access token")
                    .build();

        } catch (Exception e) {
            log.error("Error processing email line: {}", e.getMessage(), e);
            return HotmailGetCodeResponseDTO.builder()
                    .email(emailLine.split("\\|")[0])
                    .status(false)
                    .content("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Refresh token for Graph API
     */
    private TokenResult refreshAccessTokenGraph(String refreshToken, String clientId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("refresh_token", refreshToken);
            body.add("grant_type", "refresh_token");
            body.add("scope", "https://graph.microsoft.com/.default offline_access");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    GRAPH_TOKEN_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String accessToken = json.path("access_token").asText();
                String responseBody = response.getBody();
                boolean isGraphToken = responseBody.contains("Mail.Read") || responseBody.contains("Mail.ReadWrite");

                return new TokenResult(accessToken, isGraphToken);
            }
        } catch (Exception e) {
            log.warn("Failed to refresh Graph token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Refresh token for IMAP
     */
    private TokenResult refreshAccessTokenImap(String refreshToken, String clientId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("refresh_token", refreshToken);
            body.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    IMAP_TOKEN_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String accessToken = json.path("access_token").asText();
                return new TokenResult(accessToken, false);
            }
        } catch (Exception e) {
            log.warn("Failed to refresh IMAP token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Read emails using Microsoft Graph API - new format returning single result
     */
    private HotmailGetCodeResponseDTO readMailByGraphNew(String accessToken, String emailAddr, String password, List<String> emailTypes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    GRAPH_MESSAGES_URL + "?$top=50&$orderby=receivedDateTime desc",
                    HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode messages = json.path("value");

                for (JsonNode msg : messages) {
                    String fromAddr = msg.path("from").path("emailAddress").path("address").asText();
                    String subject = msg.path("subject").asText();
                    String receivedDateTime = msg.path("receivedDateTime").asText();

                    // Filter by email types
                    if (!matchesEmailTypes(fromAddr, subject, emailTypes)) {
                        continue;
                    }

                    // Extract code from subject
                    String code = extractCode(subject);
                    if (code != null && !code.isEmpty()) {
                        LocalDateTime dateTime = parseDateTime(receivedDateTime);
                        String formattedDate = dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
                        
                        return HotmailGetCodeResponseDTO.builder()
                                .email(emailAddr)
                                .password(password)
                                .status(true)
                                .code(code)
                                .content(subject)
                                .date(formattedDate)
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading mail by Graph API: {}", e.getMessage(), e);
        }

        return HotmailGetCodeResponseDTO.builder()
                .email(emailAddr)
                .password(password)
                .status(false)
                .content("No verification code found")
                .build();
    }

    /**
     * Read emails using IMAP with OAuth2 - new format returning single result
     */
    private HotmailGetCodeResponseDTO readMailByImapNew(String emailAddr, String password, String accessToken, List<String> emailTypes) {
        Store store = null;
        Folder inbox = null;

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", IMAP_HOST);
            props.put("mail.imaps.port", String.valueOf(IMAP_PORT));
            props.put("mail.imaps.ssl.enable", "true");
            props.put("mail.imaps.auth.mechanisms", "XOAUTH2");

            Session session = Session.getInstance(props);
            store = session.getStore("imaps");
            store.connect(IMAP_HOST, emailAddr, accessToken);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int messageCount = inbox.getMessageCount();
            int start = Math.max(1, messageCount - 49);
            Message[] messages = inbox.getMessages(start, messageCount);

            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                String subject = msg.getSubject();
                String from = getFromAddress(msg);
                Date sentDate = msg.getSentDate();

                if (!matchesEmailTypes(from, subject, emailTypes)) {
                    continue;
                }

                String code = extractCode(subject);
                if (code != null && !code.isEmpty()) {
                    String formattedDate = "";
                    if (sentDate != null) {
                        LocalDateTime dateTime = LocalDateTime.ofInstant(sentDate.toInstant(), ZoneId.systemDefault());
                        formattedDate = dateTime.format(DATE_FORMATTER);
                    }
                    
                    return HotmailGetCodeResponseDTO.builder()
                            .email(emailAddr)
                            .password(password)
                            .status(true)
                            .code(code)
                            .content(subject)
                            .date(formattedDate)
                            .build();
                }
            }

        } catch (Exception e) {
            log.error("Error reading mail by IMAP: {}", e.getMessage(), e);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (Exception ignored) {}
        }

        return HotmailGetCodeResponseDTO.builder()
                .email(emailAddr)
                .password(password)
                .status(false)
                .content("No verification code found")
                .build();
    }

    /**
     * Check if email matches any of the specified type filters
     */
    private boolean matchesEmailTypes(String from, String subject, List<String> emailTypes) {
        if (emailTypes == null || emailTypes.isEmpty() || emailTypes.contains("Auto")) {
            return true;
        }
        
        String combined = (from + " " + subject).toLowerCase();
        
        for (String type : emailTypes) {
            String typeLower = type.toLowerCase();
            boolean matches = switch (typeLower) {
                case "facebook" -> combined.contains("facebook") || combined.contains("fb.com");
                case "instagram" -> combined.contains("instagram");
                case "twitter" -> combined.contains("twitter") || combined.contains("x.com");
                case "apple" -> combined.contains("apple");
                case "tiktok" -> combined.contains("tiktok");
                case "amazon" -> combined.contains("amazon");
                case "lazada" -> combined.contains("lazada");
                case "shopee" -> combined.contains("shopee");
                case "kakaotalk" -> combined.contains("kakao");
                case "telegram" -> combined.contains("telegram");
                case "google" -> combined.contains("google");
                case "wechat" -> combined.contains("wechat") || combined.contains("weixin");
                default -> true;
            };
            if (matches) return true;
        }
        
        return false;
    }

    /**
     * Extract verification code from text using regex
     */
    private String extractCode(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Matcher matcher = CODE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Get from address from message
     */
    private String getFromAddress(Message message) {
        try {
            Address[] fromAddresses = message.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                return fromAddresses[0].toString();
            }
        } catch (Exception e) {
            log.warn("Error getting from address: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Parse ISO datetime string to LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr.replace("Z", ""));
        } catch (Exception e) {
            log.warn("Error parsing datetime: {}", dateTimeStr);
            return null;
        }
    }

    /**
     * Token result holder
     */
    private static class TokenResult {
        final String accessToken;
        final boolean isGraphToken;

        TokenResult(String accessToken, boolean isGraphToken) {
            this.accessToken = accessToken;
            this.isGraphToken = isGraphToken;
        }
    }
}
