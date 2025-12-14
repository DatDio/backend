package com.mailshop_dragonvu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailshop_dragonvu.dto.hotmail.*;
import com.mailshop_dragonvu.service.HotmailService;
import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of HotmailService using WebClient for non-blocking HTTP requests
 * Provides better performance and scalability compared to RestTemplate + ThreadPool
 * Note: IMAP operations remain blocking as Jakarta Mail doesn't support reactive
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HotmailServiceImpl implements HotmailService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Concurrency limit for parallel requests
    private static final int MAX_CONCURRENT_REQUESTS = 20;

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

    // ==================== TOKEN REFRESH METHODS (WebClient) ====================

    /**
     * Refresh token for Graph API using WebClient (non-blocking)
     * Returns new access_token AND new refresh_token
     */
    private Mono<TokenResult> refreshAccessTokenGraphReactive(String refreshToken, String clientId) {
        return webClient.post()
                .uri(GRAPH_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("refresh_token", refreshToken)
                        .with("grant_type", "refresh_token")
                        .with("scope", "https://graph.microsoft.com/.default offline_access"))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .map(body -> {
                                    try {
                                        JsonNode json = objectMapper.readTree(body);
                                        String accessToken = json.path("access_token").asText();
                                        String newRefreshToken = json.path("refresh_token").asText();
                                        boolean isGraphToken = body.contains("Mail.Read") || body.contains("Mail.ReadWrite");
                                        return new TokenResult(accessToken, newRefreshToken, isGraphToken);
                                    } catch (Exception e) {
                                        log.warn("Failed to parse Graph token response: {}", e.getMessage());
                                        return null;
                                    }
                                });
                    } else {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.debug("Graph token refresh failed: {} - {}", response.statusCode(), body))
                                .then(Mono.empty());
                    }
                })
                .onErrorResume(e -> {
                    log.warn("Failed to refresh Graph token: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Refresh token for IMAP using WebClient (non-blocking)
     * Returns new access_token AND new refresh_token
     */
    private Mono<TokenResult> refreshAccessTokenImapReactive(String refreshToken, String clientId) {
        return webClient.post()
                .uri(IMAP_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("refresh_token", refreshToken)
                        .with("grant_type", "refresh_token"))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .map(body -> {
                                    try {
                                        JsonNode json = objectMapper.readTree(body);
                                        String accessToken = json.path("access_token").asText();
                                        String newRefreshToken = json.path("refresh_token").asText();
                                        return new TokenResult(accessToken, newRefreshToken, false);
                                    } catch (Exception e) {
                                        log.warn("Failed to parse IMAP token response: {}", e.getMessage());
                                        return null;
                                    }
                                });
                    } else {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.debug("IMAP token refresh failed: {} - {}", response.statusCode(), body))
                                .then(Mono.empty());
                    }
                })
                .onErrorResume(e -> {
                    log.warn("Failed to refresh IMAP token: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    // ==================== MAIL READ METHODS ====================

    /**
     * Read emails using Microsoft Graph API (non-blocking with WebClient)
     */
    private Mono<HotmailGetCodeResponseDTO> readMailByGraphReactive(String accessToken, String emailAddr, String password, List<String> emailTypes) {
        return webClient.get()
                .uri(GRAPH_MESSAGES_URL + "?$top=50&$orderby=receivedDateTime desc")
                .headers(h -> h.setBearerAuth(accessToken))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .map(body -> parseGraphMessages(body, emailAddr, password, emailTypes));
                    } else {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.debug("Graph messages read failed: {} - {}", response.statusCode(), body))
                                .thenReturn(createNoCodeResult(emailAddr, password));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error reading mail by Graph API: {}", e.getMessage());
                    return Mono.just(createNoCodeResult(emailAddr, password));
                });
    }

    /**
     * Parse Graph API messages response
     */
    private HotmailGetCodeResponseDTO parseGraphMessages(String responseBody, String emailAddr, String password, List<String> emailTypes) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
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
        } catch (Exception e) {
            log.error("Error parsing Graph messages: {}", e.getMessage());
        }

        return createNoCodeResult(emailAddr, password);
    }

    /**
     * Read emails using IMAP with OAuth2 (blocking - wrapped in Mono)
     * IMAP operations are inherently blocking, so we run on boundedElastic scheduler
     */
    private Mono<HotmailGetCodeResponseDTO> readMailByImapReactive(String emailAddr, String password, String accessToken, List<String> emailTypes) {
        return Mono.fromCallable(() -> readMailByImapBlocking(emailAddr, password, accessToken, emailTypes))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Blocking IMAP read operation
     */
    private HotmailGetCodeResponseDTO readMailByImapBlocking(String emailAddr, String password, String accessToken, List<String> emailTypes) {
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
            log.error("Error reading mail by IMAP: {}", e.getMessage());
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (Exception ignored) {}
        }

        return createNoCodeResult(emailAddr, password);
    }

    /**
     * Create a no-code-found result
     */
    private HotmailGetCodeResponseDTO createNoCodeResult(String emailAddr, String password) {
        return HotmailGetCodeResponseDTO.builder()
                .email(emailAddr)
                .password(password)
                .status(false)
                .content("No verification code found")
                .build();
    }

    // ==================== HELPER METHODS ====================

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

    // ==================== SSE STREAMING METHODS (Reactive) ====================

    /**
     * Get verification code with SSE streaming using reactive Flux
     */
    @Override
    public void getCodeStream(HotmailGetCodeRequestDTO request, SseEmitter emitter) {
        if (request.getEmailData() == null || request.getEmailData().isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        List<String> emailLines = Arrays.stream(request.getEmailData().trim().split("\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        if (emailLines.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        int total = emailLines.size();

        // Setup error/timeout handlers
        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE timeout"));

        // Process all emails using reactive streams with concurrency control
        Flux.fromIterable(emailLines)
                .flatMapSequential(line -> processGetCodeSingleEmailReactive(line, request)
                                .subscribeOn(Schedulers.boundedElastic()),
                        MAX_CONCURRENT_REQUESTS)
                .doOnNext(result -> {
                    // Set CheckStatus based on result
                    if (result.isStatus() && result.getCode() != null && !result.getCode().isEmpty()) {
                        result.setCheckStatus(CheckStatus.SUCCESS);
                    } else if (result.getContent() != null && result.getContent().contains("Error")) {
                        result.setCheckStatus(CheckStatus.UNKNOWN);
                    } else {
                        result.setCheckStatus(CheckStatus.FAILED);
                    }

                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(result));
                    } catch (IOException e) {
                        log.error("Error sending SSE for get-code: {}", e.getMessage());
                    }

                    if (completed.incrementAndGet() == total) {
                        sendCompleteEvent(emitter);
                    }
                })
                .doOnError(error -> {
                    log.error("Stream error: {}", error.getMessage());
                    completeEmitter(emitter);
                })
                .doOnComplete(() -> log.info("Get-code completed for {} emails", total))
                .subscribe();
    }

    /**
     * Process a single email line and get verification code (reactive)
     */
    private Mono<HotmailGetCodeResponseDTO> processGetCodeSingleEmailReactive(String emailLine, HotmailGetCodeRequestDTO request) {
        try {
            // Parse email data: email|password|refresh_token|client_id
            String[] parts = emailLine.split("\\|");
            if (parts.length < 3) {
                log.error("Invalid email data format for line: {}", emailLine);
                return Mono.just(HotmailGetCodeResponseDTO.builder()
                        .email(parts.length > 0 ? parts[0] : emailLine)
                        .password(parts.length > 1 ? parts[1] : "")
                        .refreshToken("")
                        .clientId("")
                        .status(false)
                        .content("Invalid format: requires email|password|refresh_token|client_id")
                        .build());
            }

            String emailAddr = parts[0];
            String password = parts[1];
            String refreshToken = parts.length > 2 ? parts[2] : "";
            String clientId = parts.length > 3 && !parts[3].isEmpty() ? parts[3] : DEFAULT_CLIENT_ID;

            List<String> emailTypes = request.getEmailTypes();
            if (emailTypes == null || emailTypes.isEmpty()) {
                emailTypes = List.of("Auto");
            }
            final List<String> finalEmailTypes = emailTypes;

            // Try Graph API first
            if ("Oauth2".equalsIgnoreCase(request.getGetType()) || "Graph API".equalsIgnoreCase(request.getGetType())) {
                return refreshAccessTokenGraphReactive(refreshToken, clientId)
                        .flatMap(graphToken -> {
                            if (graphToken != null && graphToken.isGraphToken) {
                                log.info("Reading mail using Graph API for: {}", emailAddr);
                                return readMailByGraphReactive(graphToken.accessToken, emailAddr, password, finalEmailTypes)
                                        .map(result -> {
                                            result.setRefreshToken(refreshToken);
                                            result.setClientId(clientId);
                                            return result;
                                        });
                            }
                            return Mono.empty();
                        })
                        .switchIfEmpty(
                            // Fallback to IMAP with OAuth
                            refreshAccessTokenImapReactive(refreshToken, clientId)
                                    .flatMap(imapToken -> {
                                        if (imapToken != null) {
                                            log.info("Reading mail using IMAP OAuth for: {}", emailAddr);
                                            return readMailByImapReactive(emailAddr, password, imapToken.accessToken, finalEmailTypes)
                                                    .map(result -> {
                                                        result.setRefreshToken(refreshToken);
                                                        result.setClientId(clientId);
                                                        return result;
                                                    });
                                        }
                                        return Mono.empty();
                                    })
                        )
                        .switchIfEmpty(Mono.just(HotmailGetCodeResponseDTO.builder()
                                .email(emailAddr)
                                .password(password)
                                .refreshToken(refreshToken)
                                .clientId(clientId)
                                .status(false)
                                .content("Could not get access token")
                                .build()));
            }

            // Default response if getType not matched
            return Mono.just(HotmailGetCodeResponseDTO.builder()
                    .email(emailAddr)
                    .password(password)
                    .refreshToken(refreshToken)
                    .clientId(clientId)
                    .status(false)
                    .content("Unsupported get type")
                    .build());

        } catch (Exception e) {
            log.error("Error processing email line: {}", e.getMessage(), e);
            String[] errorParts = emailLine.split("\\|");
            return Mono.just(HotmailGetCodeResponseDTO.builder()
                    .email(errorParts[0])
                    .password(errorParts.length > 1 ? errorParts[1] : "")
                    .refreshToken(errorParts.length > 2 ? errorParts[2] : "")
                    .clientId(errorParts.length > 3 ? errorParts[3] : "")
                    .status(false)
                    .content("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Check live mail with SSE streaming using reactive Flux
     */
    @Override
    public void checkLiveMailStream(String emailData, SseEmitter emitter) {
        if (emailData == null || emailData.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        List<String> emailLines = Arrays.stream(emailData.trim().split("\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        if (emailLines.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        int total = emailLines.size();

        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE timeout"));

        Flux.fromIterable(emailLines)
                .flatMapSequential(line -> checkLiveSingleMailReactive(line)
                                .subscribeOn(Schedulers.boundedElastic()),
                        MAX_CONCURRENT_REQUESTS)
                .doOnNext(result -> {
                    // Set CheckStatus based on isLive and error
                    if (result.isLive()) {
                        result.setStatus(CheckStatus.SUCCESS);
                    } else if (result.getError() != null && (result.getError().contains("Error") || result.getError().contains("exception"))) {
                        result.setStatus(CheckStatus.UNKNOWN);
                    } else {
                        result.setStatus(CheckStatus.FAILED);
                    }

                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(result));
                    } catch (IOException e) {
                        log.error("Error sending SSE for check-live-mail: {}", e.getMessage());
                    }

                    if (completed.incrementAndGet() == total) {
                        sendCompleteEvent(emitter);
                    }
                })
                .doOnError(error -> {
                    log.error("Stream error: {}", error.getMessage());
                    completeEmitter(emitter);
                })
                .doOnComplete(() -> log.info("Check-live-mail completed for {} emails", total))
                .subscribe();
    }

    /**
     * Check a single email line for live status (reactive)
     */
    private Mono<CheckLiveMailResponseDTO> checkLiveSingleMailReactive(String emailLine) {
        try {
            String[] parts = emailLine.split("\\|");
            if (parts.length < 3) {
                return Mono.just(CheckLiveMailResponseDTO.builder()
                        .email(parts.length > 0 ? parts[0] : emailLine)
                        .password(parts.length > 1 ? parts[1] : "")
                        .refreshToken("")
                        .clientId("")
                        .isLive(false)
                        .error("Invalid format: requires email|password|refresh_token|client_id")
                        .build());
            }

            String email = parts[0].trim();
            String password = parts[1].trim();
            String refreshToken = parts[2].trim();
            String clientId = parts.length > 3 && !parts[3].isEmpty() ? parts[3].trim() : DEFAULT_CLIENT_ID;

            // Try to refresh token - if successful, email is live
            return refreshAccessTokenGraphReactive(refreshToken, clientId)
                    .filter(token -> token != null && token.accessToken != null && !token.accessToken.isEmpty())
                    .map(token -> CheckLiveMailResponseDTO.builder()
                            .email(email)
                            .password(password)
                            .refreshToken(refreshToken)
                            .clientId(clientId)
                            .isLive(true)
                            .build())
                    .switchIfEmpty(
                        // Try IMAP token as fallback
                        refreshAccessTokenImapReactive(refreshToken, clientId)
                                .filter(token -> token != null && token.accessToken != null && !token.accessToken.isEmpty())
                                .map(token -> CheckLiveMailResponseDTO.builder()
                                        .email(email)
                                        .password(password)
                                        .refreshToken(refreshToken)
                                        .clientId(clientId)
                                        .isLive(true)
                                        .build())
                    )
                    .switchIfEmpty(Mono.just(CheckLiveMailResponseDTO.builder()
                            .email(email)
                            .password(password)
                            .refreshToken(refreshToken)
                            .clientId(clientId)
                            .isLive(false)
                            .error("Token refresh failed")
                            .build()));

        } catch (Exception e) {
            log.error("Error checking mail live status: {}", e.getMessage());
            return Mono.just(CheckLiveMailResponseDTO.builder()
                    .email(emailLine.split("\\|")[0])
                    .isLive(false)
                    .error("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Get OAuth2 token with SSE streaming using reactive Flux
     */
    @Override
    public void getOAuth2Stream(String emailData, SseEmitter emitter) {
        if (emailData == null || emailData.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        List<String> emailLines = Arrays.stream(emailData.trim().split("\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        if (emailLines.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        int total = emailLines.size();

        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE timeout"));

        Flux.fromIterable(emailLines)
                .flatMapSequential(line -> getOAuth2ForSingleMailReactive(line)
                                .subscribeOn(Schedulers.boundedElastic()),
                        MAX_CONCURRENT_REQUESTS)
                .doOnNext(result -> {
                    // Set CheckStatus based on success and error
                    if (result.isSuccess() && result.getAccessToken() != null) {
                        result.setStatus(CheckStatus.SUCCESS);
                    } else if (result.getError() != null && (result.getError().contains("Error") || result.getError().contains("exception"))) {
                        result.setStatus(CheckStatus.UNKNOWN);
                    } else {
                        result.setStatus(CheckStatus.FAILED);
                    }

                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(result));
                    } catch (IOException e) {
                        log.error("Error sending SSE for get-oauth2: {}", e.getMessage());
                    }

                    if (completed.incrementAndGet() == total) {
                        sendCompleteEvent(emitter);
                    }
                })
                .doOnError(error -> {
                    log.error("Stream error: {}", error.getMessage());
                    completeEmitter(emitter);
                })
                .doOnComplete(() -> log.info("Get-oauth2 completed for {} emails", total))
                .subscribe();
    }

    /**
     * Renew refresh token for a single email line (reactive)
     * Returns new refresh token along with access token
     */
    private Mono<GetOAuth2ResponseDTO> getOAuth2ForSingleMailReactive(String emailLine) {
        try {
            String[] parts = emailLine.split("\\|");
            if (parts.length < 3) {
                return Mono.just(GetOAuth2ResponseDTO.builder()
                        .email(parts.length > 0 ? parts[0] : emailLine)
                        .password(parts.length > 1 ? parts[1] : "")
                        .refreshToken("")
                        .clientId("")
                        .success(false)
                        .error("Invalid format: requires email|password|refresh_token|client_id")
                        .build());
            }

            String email = parts[0].trim();
            String password = parts[1].trim();
            String refreshToken = parts[2].trim();
            String clientId = parts.length > 3 && !parts[3].isEmpty() ? parts[3].trim() : DEFAULT_CLIENT_ID;

            // Try Graph API token first - MUST have new refresh token to be success
            return refreshAccessTokenGraphReactive(refreshToken, clientId)
                    .filter(token -> token != null 
                            && token.accessToken != null && !token.accessToken.isEmpty()
                            && token.newRefreshToken != null && !token.newRefreshToken.isEmpty())
                    .map(token -> {
                        String fullData = email + "|" + password + "|" + token.newRefreshToken + "|" + clientId;
                        return GetOAuth2ResponseDTO.builder()
                                .email(email)
                                .password(password)
                                .refreshToken(token.newRefreshToken)
                                .clientId(clientId)
                                .accessToken(token.accessToken)
                                .fullData(fullData)
                                .success(true)
                                .build();
                    })
                    .switchIfEmpty(
                        // Try IMAP token as fallback - MUST have new refresh token
                        refreshAccessTokenImapReactive(refreshToken, clientId)
                                .filter(token -> token != null 
                                        && token.accessToken != null && !token.accessToken.isEmpty()
                                        && token.newRefreshToken != null && !token.newRefreshToken.isEmpty())
                                .map(token -> {
                                    String fullData = email + "|" + password + "|" + token.newRefreshToken + "|" + clientId;
                                    return GetOAuth2ResponseDTO.builder()
                                            .email(email)
                                            .password(password)
                                            .refreshToken(token.newRefreshToken)
                                            .clientId(clientId)
                                            .accessToken(token.accessToken)
                                            .fullData(fullData)
                                            .success(true)
                                            .build();
                                })
                    )
                    .switchIfEmpty(Mono.just(GetOAuth2ResponseDTO.builder()
                            .email(email)
                            .password(password)
                            .refreshToken(refreshToken)  // Return original token on failure for retry
                            .clientId(clientId)
                            .success(false)
                            .error("Could not get new refresh token")
                            .build()));

        } catch (Exception e) {
            log.error("Error renewing refresh token: {}", e.getMessage());
            return Mono.just(GetOAuth2ResponseDTO.builder()
                    .email(emailLine.split("\\|")[0])
                    .success(false)
                    .error("Error: " + e.getMessage())
                    .build());
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Send completion event and complete emitter
     */
    private void sendCompleteEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data("complete"));
            emitter.complete();
        } catch (Exception e) {
            log.debug("Error sending complete event: {}", e.getMessage());
        }
    }

    /**
     * Safely complete emitter
     */
    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    // ==================== READ MAIL STREAM ====================

    /**
     * Read mailbox with SSE streaming using reactive Flux
     */
    @Override
    public void readMailStream(ReadMailRequestDTO request, SseEmitter emitter) {
        if (request.getEmailData() == null || request.getEmailData().isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        List<String> emailLines = Arrays.stream(request.getEmailData().trim().split("\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        if (emailLines.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        int total = emailLines.size();
        int messageCount = request.getMessageCount() > 0 ? request.getMessageCount() : 20;

        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE timeout"));

        Flux.fromIterable(emailLines)
                .flatMapSequential(line -> readMailForSingleEmailReactive(line, messageCount)
                                .subscribeOn(Schedulers.boundedElastic()),
                        MAX_CONCURRENT_REQUESTS)
                .doOnNext(result -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(result));
                    } catch (IOException e) {
                        log.error("Error sending SSE for read-mail: {}", e.getMessage());
                    }

                    if (completed.incrementAndGet() == total) {
                        sendCompleteEvent(emitter);
                    }
                })
                .doOnError(error -> {
                    log.error("Stream error: {}", error.getMessage());
                    completeEmitter(emitter);
                })
                .doOnComplete(() -> log.info("Read-mail completed for {} emails", total))
                .subscribe();
    }

    /**
     * Read mailbox for a single email (reactive)
     */
    private Mono<ReadMailResponseDTO> readMailForSingleEmailReactive(String emailLine, int messageCount) {
        try {
            String[] parts = emailLine.split("\\|");
            if (parts.length < 3) {
                return Mono.just(ReadMailResponseDTO.builder()
                        .email(parts.length > 0 ? parts[0] : emailLine)
                        .password(parts.length > 1 ? parts[1] : "")
                        .success(false)
                        .status(CheckStatus.FAILED)
                        .error("Invalid format: requires email|password|refresh_token|client_id")
                        .build());
            }

            String email = parts[0].trim();
            String password = parts[1].trim();
            String refreshToken = parts[2].trim();
            String clientId = parts.length > 3 && !parts[3].isEmpty() ? parts[3].trim() : DEFAULT_CLIENT_ID;

            // Try Graph API first
            return refreshAccessTokenGraphReactive(refreshToken, clientId)
                    .filter(token -> token != null && token.isGraphToken)
                    .flatMap(token -> readMailboxByGraphReactive(token.accessToken, email, password, messageCount))
                    .switchIfEmpty(
                        // Fallback to IMAP
                        refreshAccessTokenImapReactive(refreshToken, clientId)
                                .filter(token -> token != null && token.accessToken != null)
                                .flatMap(token -> readMailboxByImapReactive(email, password, token.accessToken, messageCount))
                    )
                    .switchIfEmpty(Mono.just(ReadMailResponseDTO.builder()
                            .email(email)
                            .password(password)
                            .success(false)
                            .status(CheckStatus.FAILED)
                            .error("Could not get access token")
                            .build()));

        } catch (Exception e) {
            log.error("Error reading mailbox: {}", e.getMessage());
            return Mono.just(ReadMailResponseDTO.builder()
                    .email(emailLine.split("\\|")[0])
                    .success(false)
                    .status(CheckStatus.UNKNOWN)
                    .error("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Read mailbox using Graph API (non-blocking)
     */
    private Mono<ReadMailResponseDTO> readMailboxByGraphReactive(String accessToken, String email, String password, int messageCount) {
        return webClient.get()
                .uri(GRAPH_MESSAGES_URL + "?$top=" + messageCount + "&$orderby=receivedDateTime desc&$select=subject,from,bodyPreview,body,receivedDateTime,isRead,hasAttachments")
                .headers(h -> h.setBearerAuth(accessToken))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .map(body -> parseMailboxGraphResponse(body, email, password));
                    } else {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.debug("Graph mailbox read failed: {} - {}", response.statusCode(), body))
                                .thenReturn(ReadMailResponseDTO.builder()
                                        .email(email)
                                        .password(password)
                                        .success(false)
                                        .status(CheckStatus.FAILED)
                                        .error("Graph API error: " + response.statusCode())
                                        .build());
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error reading mailbox by Graph API: {}", e.getMessage());
                    return Mono.just(ReadMailResponseDTO.builder()
                            .email(email)
                            .password(password)
                            .success(false)
                            .status(CheckStatus.UNKNOWN)
                            .error("Error: " + e.getMessage())
                            .build());
                });
    }

    /**
     * Parse Graph API mailbox response
     */
    private ReadMailResponseDTO parseMailboxGraphResponse(String responseBody, String email, String password) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            JsonNode messagesNode = json.path("value");
            
            List<ReadMailResponseDTO.EmailMessage> messages = new ArrayList<>();
            
            for (JsonNode msg : messagesNode) {
                String subject = msg.path("subject").asText("");
                String fromAddr = msg.path("from").path("emailAddress").path("address").asText("");
                String fromName = msg.path("from").path("emailAddress").path("name").asText("");
                String preview = msg.path("bodyPreview").asText("");
                String htmlBody = msg.path("body").path("content").asText("");
                String receivedDateTime = msg.path("receivedDateTime").asText("");
                boolean isRead = msg.path("isRead").asBoolean(false);
                boolean hasAttachments = msg.path("hasAttachments").asBoolean(false);
                
                LocalDateTime dateTime = parseDateTime(receivedDateTime);
                String formattedDate = dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
                
                // Format from as "Name <email>" or just email
                String from = fromName.isEmpty() ? fromAddr : fromName + " <" + fromAddr + ">";
                
                messages.add(ReadMailResponseDTO.EmailMessage.builder()
                        .subject(subject)
                        .from(from)
                        .preview(preview.length() > 200 ? preview.substring(0, 200) + "..." : preview)
                        .htmlBody(htmlBody)
                        .date(formattedDate)
                        .isRead(isRead)
                        .hasAttachments(hasAttachments)
                        .build());
            }
            
            return ReadMailResponseDTO.builder()
                    .email(email)
                    .password(password)
                    .success(true)
                    .status(CheckStatus.SUCCESS)
                    .messages(messages)
                    .totalMessages(messages.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error parsing Graph mailbox response: {}", e.getMessage());
            return ReadMailResponseDTO.builder()
                    .email(email)
                    .password(password)
                    .success(false)
                    .status(CheckStatus.UNKNOWN)
                    .error("Parse error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Read mailbox using IMAP (blocking - wrapped in Mono)
     */
    private Mono<ReadMailResponseDTO> readMailboxByImapReactive(String email, String password, String accessToken, int messageCount) {
        return Mono.fromCallable(() -> readMailboxByImapBlocking(email, password, accessToken, messageCount))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Blocking IMAP mailbox read
     */
    private ReadMailResponseDTO readMailboxByImapBlocking(String email, String password, String accessToken, int messageCount) {
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
            store.connect(IMAP_HOST, email, accessToken);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int totalMessages = inbox.getMessageCount();
            int start = Math.max(1, totalMessages - messageCount + 1);
            Message[] messages = inbox.getMessages(start, totalMessages);
            
            List<ReadMailResponseDTO.EmailMessage> emailMessages = new ArrayList<>();
            
            // Read messages in reverse order (newest first)
            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                String subject = msg.getSubject() != null ? msg.getSubject() : "(No Subject)";
                String from = getFromAddress(msg);
                Date sentDate = msg.getSentDate();
                boolean isRead = msg.isSet(Flags.Flag.SEEN);
                
                String formattedDate = "";
                if (sentDate != null) {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(sentDate.toInstant(), ZoneId.systemDefault());
                    formattedDate = dateTime.format(DATE_FORMATTER);
                }
                
                // Get preview (first 200 chars of content)
                String preview = "";
                try {
                    Object content = msg.getContent();
                    if (content instanceof String) {
                        preview = ((String) content).substring(0, Math.min(200, ((String) content).length()));
                    } else if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        if (mp.getCount() > 0) {
                            BodyPart bp = mp.getBodyPart(0);
                            if (bp.getContent() instanceof String) {
                                String text = (String) bp.getContent();
                                preview = text.substring(0, Math.min(200, text.length()));
                            }
                        }
                    }
                } catch (Exception ignored) {}
                
                emailMessages.add(ReadMailResponseDTO.EmailMessage.builder()
                        .subject(subject)
                        .from(from)
                        .preview(preview.length() > 200 ? preview.substring(0, 200) + "..." : preview)
                        .date(formattedDate)
                        .isRead(isRead)
                        .hasAttachments(false) // IMAP doesn't easily expose this
                        .build());
            }
            
            return ReadMailResponseDTO.builder()
                    .email(email)
                    .password(password)
                    .success(true)
                    .status(CheckStatus.SUCCESS)
                    .messages(emailMessages)
                    .totalMessages(totalMessages)
                    .build();

        } catch (Exception e) {
            log.error("Error reading mailbox by IMAP: {}", e.getMessage());
            return ReadMailResponseDTO.builder()
                    .email(email)
                    .password(password)
                    .success(false)
                    .status(CheckStatus.UNKNOWN)
                    .error("IMAP error: " + e.getMessage())
                    .build();
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) inbox.close(false);
                if (store != null && store.isConnected()) store.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Token result holder
     */
    private static class TokenResult {
        final String accessToken;
        final String newRefreshToken;
        final boolean isGraphToken;

        TokenResult(String accessToken, String newRefreshToken, boolean isGraphToken) {
            this.accessToken = accessToken;
            this.newRefreshToken = newRefreshToken;
            this.isGraphToken = isGraphToken;
        }
    }
}
