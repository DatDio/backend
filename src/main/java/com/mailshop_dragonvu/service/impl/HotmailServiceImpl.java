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

    @Override
    public List<HotmailGetCodeResponseDTO> getCode(HotmailGetCodeRequestDTO request) {
        try {
            // Parse email data: email|password|refresh_token|client_id
            String[] parts = request.getEmailData().trim().split("\\|");
            if (parts.length < 3) {
                log.error("Invalid email data format. Expected: email|password|refresh_token|client_id");
                return Collections.emptyList();
            }

            String emailAddr = parts[0];
            // parts[1] is password - not used for OAuth but kept for compatibility
            String refreshToken = parts.length > 2 ? parts[2] : "";
            String clientId = parts.length > 3 && !parts[3].isEmpty() ? parts[3] : DEFAULT_CLIENT_ID;

            // Try Graph API first (OAuth2)
            if ("Oauth2".equalsIgnoreCase(request.getGetType())) {
                TokenResult graphToken = refreshAccessTokenGraph(refreshToken, clientId);
                if (graphToken != null && graphToken.isGraphToken) {
                    log.info("Reading mail using Graph API for: {}", emailAddr);
                    return readMailByGraph(graphToken.accessToken, emailAddr, request.getEmailType());
                }

                // Fallback to IMAP with OAuth
                TokenResult imapToken = refreshAccessTokenImap(refreshToken, clientId);
                if (imapToken != null) {
                    log.info("Reading mail using IMAP OAuth for: {}", emailAddr);
                    return readMailByImap(emailAddr, imapToken.accessToken, request.getEmailType());
                }
            }

            // POP3/Basic auth not supported for Hotmail OAuth
            log.warn("Could not get access token for: {}", emailAddr);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error reading Hotmail: {}", e.getMessage(), e);
            return Collections.emptyList();
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
     * Read emails using Microsoft Graph API
     */
    private List<HotmailGetCodeResponseDTO> readMailByGraph(String accessToken, String emailAddr, String emailType) {
        List<HotmailGetCodeResponseDTO> emails = new ArrayList<>();

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

                    // Filter by email type if needed
                    if (!matchesEmailType(fromAddr, subject, emailType)) {
                        continue;
                    }

                    // Extract code from subject
                    String code = extractCode(subject);
                    if (code != null && !code.isEmpty()) {
                        HotmailGetCodeResponseDTO dto = HotmailGetCodeResponseDTO.builder()
                                .email(emailAddr)
                                .code(code)
                                .from(fromAddr)
                                .subject(subject)
                                .receivedAt(parseDateTime(receivedDateTime))
                                .build();
                        emails.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading mail by Graph API: {}", e.getMessage(), e);
        }

        return emails;
    }

    /**
     * Read emails using IMAP with OAuth2
     */
    private List<HotmailGetCodeResponseDTO> readMailByImap(String emailAddr, String accessToken, String emailType) {
        List<HotmailGetCodeResponseDTO> emails = new ArrayList<>();
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

            // Connect using OAuth2
            store.connect(IMAP_HOST, emailAddr, accessToken);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Get last 50 messages
            int messageCount = inbox.getMessageCount();
            int start = Math.max(1, messageCount - 49);
            Message[] messages = inbox.getMessages(start, messageCount);

            // Process in reverse order (newest first)
            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                String subject = msg.getSubject();
                String from = getFromAddress(msg);
                Date sentDate = msg.getSentDate();

                // Filter by email type if needed
                if (!matchesEmailType(from, subject, emailType)) {
                    continue;
                }

                // Extract code from subject
                String code = extractCode(subject);
                if (code != null && !code.isEmpty()) {
                    HotmailGetCodeResponseDTO dto = HotmailGetCodeResponseDTO.builder()
                            .email(emailAddr)
                            .code(code)
                            .from(from)
                            .subject(subject)
                            .receivedAt(sentDate != null
                                    ? LocalDateTime.ofInstant(sentDate.toInstant(), ZoneId.systemDefault())
                                    : null)
                            .build();
                    emails.add(dto);
                }
            }

        } catch (Exception e) {
            log.error("Error reading mail by IMAP: {}", e.getMessage(), e);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                log.warn("Error closing IMAP connection: {}", e.getMessage());
            }
        }

        return emails;
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
     * Check if email matches the specified type filter
     */
    private boolean matchesEmailType(String from, String subject, String emailType) {
        if (emailType == null || "Auto".equalsIgnoreCase(emailType)) {
            return true;
        }

        String combined = (from + " " + subject).toLowerCase();
        String type = emailType.toLowerCase();

        return switch (type) {
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
            default -> true;
        };
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
