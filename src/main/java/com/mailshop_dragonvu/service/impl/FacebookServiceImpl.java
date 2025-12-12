package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.facebook.FacebookCheckLiveResponseDTO;
import com.mailshop_dragonvu.dto.hotmail.CheckStatus;
import com.mailshop_dragonvu.service.FacebookService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of FacebookService
 * Uses multi-threading for parallel processing of Facebook UID checks
 */
@Service
@Slf4j
public class FacebookServiceImpl implements FacebookService {

    private final RestTemplate restTemplate;

    // Thread pool for parallel processing (10 threads)
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final String FACEBOOK_GRAPH_PICTURE_URL = "https://graph.facebook.com/%s/picture?type=large";

    public FacebookServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void checkLiveStream(String uidData, SseEmitter emitter) {
        if (uidData == null || uidData.isEmpty()) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
            return;
        }

        String[] uidLines = uidData.trim().split("\\n");
        AtomicInteger completed = new AtomicInteger(0);
        int total = uidLines.length;

        // Setup error/timeout handlers
        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE timeout"));

        for (String uidLine : uidLines) {
            String uid = uidLine.trim();
            if (uid.isEmpty()) {
                if (completed.incrementAndGet() == total) {
                    try {
                        emitter.complete();
                    } catch (Exception ignored) {
                    }
                }
                continue;
            }

            executor.submit(() -> {
                try {
                    FacebookCheckLiveResponseDTO result = checkSingleUid(uid);
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(result));
                } catch (IOException e) {
                    log.error("Error sending SSE for facebook check-live: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("Error processing UID for facebook check-live stream: {}", e.getMessage());
                    try {
                        FacebookCheckLiveResponseDTO errorResult = FacebookCheckLiveResponseDTO.builder()
                                .uid(uid)
                                .status(CheckStatus.UNKNOWN)
                                .error("Error: " + e.getMessage())
                                .build();
                        emitter.send(SseEmitter.event().name("result").data(errorResult));
                    } catch (IOException ignored) {
                    }
                } finally {
                    if (completed.incrementAndGet() == total) {
                        try {
                            emitter.send(SseEmitter.event().name("done").data("complete"));
                            emitter.complete();
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        }
    }

    /**
     * Check a single Facebook UID for live status
     * Uses the scontent CDN directly instead of Graph API to avoid token requirements
     */
    private FacebookCheckLiveResponseDTO checkSingleUid(String uid) {
        try {
            // Use platform-lookaside which doesn't require access token
            String url = String.format("https://www.facebook.com/photo.php?fbid=%s", uid);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.set("Accept-Language", "en-US,en;q=0.5");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String body = response.getBody();

                // Check if the page contains user profile indicators
                // If we find profile-related content, the UID exists
                if (body.contains("profile") || body.contains("fb://profile")) {
                    String avatarUrl = String.format("https://graph.facebook.com/%s/picture?type=large", uid);
                    return FacebookCheckLiveResponseDTO.builder()
                            .uid(uid)
                            .status(CheckStatus.SUCCESS)
                            .avatar(avatarUrl)
                            .build();
                }
            }

            // Fallback: Try checking through mobile basic Facebook
            return checkViaAlternativeMethod(uid);

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            log.debug("Primary check failed for UID {}: {}, trying alternative", uid, errorMsg);

            // Try alternative method
            return checkViaAlternativeMethod(uid);
        }
    }

    /**
     * Alternative method to check UID using mbasic Facebook
     */
    private FacebookCheckLiveResponseDTO checkViaAlternativeMethod(String uid) {
        try {
            // Use mbasic Facebook which is more accessible
            String url = String.format("https://mbasic.facebook.com/profile.php?id=%s", uid);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml");
            headers.set("Accept-Language", "vi-VN,vi;q=0.9,en;q=0.8");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String body = response.getBody();

                // Check for indicators that the profile exists
                // Looking for profile-specific elements that wouldn't exist on error pages
                boolean hasProfileIndicators =
                        body.contains("cover") ||
                                body.contains("timeline") ||
                                body.contains("profile_photo") ||
                                (body.contains("id=\"root\"") && !body.contains("error")) ||
                                body.contains("Thêm bạn"); // "Add friend" in Vietnamese

                boolean isErrorPage =
                        body.contains("Trang bạn yêu cầu không thể hiển thị") ||
                                body.contains("This content isn't available") ||
                                body.contains("Nội dung này hiện không có") ||
                                body.contains("Page Not Found") ||
                                body.contains("Sorry, this content isn't available");

                if (hasProfileIndicators && !isErrorPage) {
                    String avatarUrl = String.format("https://graph.facebook.com/%s/picture?type=large", uid);
                    return FacebookCheckLiveResponseDTO.builder()
                            .uid(uid)
                            .status(CheckStatus.SUCCESS)
                            .avatar(avatarUrl)
                            .build();
                } else if (isErrorPage) {
                    return FacebookCheckLiveResponseDTO.builder()
                            .uid(uid)
                            .status(CheckStatus.FAILED)
                            .error("UID không tồn tại hoặc bị khóa")
                            .build();
                }
            }

            // Unable to determine - mark as unknown
            return FacebookCheckLiveResponseDTO.builder()
                    .uid(uid)
                    .status(CheckStatus.UNKNOWN)
                    .error("Không thể xác định trạng thái")
                    .build();

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            log.warn("Alternative check failed for UID {}: {}", uid, errorMsg);

            // Mark as unknown since we couldn't verify
            return FacebookCheckLiveResponseDTO.builder()
                    .uid(uid)
                    .status(CheckStatus.UNKNOWN)
                    .error("Lỗi kiểm tra: " + (errorMsg != null ? errorMsg.substring(0, Math.min(50, errorMsg.length())) : "Unknown"))
                    .build();
        }
    }
}