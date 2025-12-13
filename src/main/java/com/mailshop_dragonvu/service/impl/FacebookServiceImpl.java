package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.facebook.FacebookCheckLiveResponseDTO;
import com.mailshop_dragonvu.dto.hotmail.CheckStatus;
import com.mailshop_dragonvu.service.FacebookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of FacebookService using WebClient for non-blocking HTTP requests
 * Provides better performance and scalability compared to RestTemplate + ThreadPool
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FacebookServiceImpl implements FacebookService {

    private final WebClient webClient;

    // Concurrency limit for parallel requests (prevent overwhelming Facebook servers)
    private static final int MAX_CONCURRENT_REQUESTS = 20;

    @Override
    public void checkLiveStream(String uidData, SseEmitter emitter) {
        if (uidData == null || uidData.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        // Parse UIDs from input
        List<String> uids = Arrays.stream(uidData.trim().split("\\n"))
                .map(String::trim)
                .filter(uid -> !uid.isEmpty())
                .toList();

        if (uids.isEmpty()) {
            completeEmitter(emitter);
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        int total = uids.size();

        // Setup error/timeout handlers
        emitter.onError(e -> log.warn("SSE error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE timeout"));

        // Process all UIDs using reactive streams with flatMap for concurrency control
        Flux.fromIterable(uids)
                .flatMap(uid -> checkSingleUidReactive(uid)
                                .subscribeOn(Schedulers.boundedElastic()),
                        MAX_CONCURRENT_REQUESTS) // Limit concurrent requests
                .doOnNext(result -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(result));
                    } catch (IOException e) {
                        log.error("Error sending SSE result: {}", e.getMessage());
                    }

                    // Check if all completed
                    if (completed.incrementAndGet() == total) {
                        sendCompleteEvent(emitter);
                    }
                })
                .doOnError(error -> {
                    log.error("Stream error: {}", error.getMessage());
                    completeEmitter(emitter);
                })
                .doOnComplete(() -> {
                    log.info("Facebook check-live completed for {} UIDs", total);
                })
                .subscribe();
    }

    /**
     * Check a single Facebook UID using WebClient (non-blocking)
     * Uses exchangeToMono to read response body even on error status codes
     */
    private Mono<FacebookCheckLiveResponseDTO> checkSingleUidReactive(String uid) {
        String url = String.format("https://graph.facebook.com/%s/picture?type=normal&redirect=false", uid);

        return webClient.get()
                .uri(url)
                .exchangeToMono(response -> {
                    // Always read the body, regardless of status code
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    // Success response - check if valid profile picture
                                    if (body.contains("height") || body.contains("width")) {
                                        return FacebookCheckLiveResponseDTO.builder()
                                                .uid(uid)
                                                .status(CheckStatus.SUCCESS)
                                                .build();
                                    } else {
                                        return FacebookCheckLiveResponseDTO.builder()
                                                .uid(uid)
                                                .status(CheckStatus.FAILED)
                                                .build();
                                    }
                                } else {
                                    // Error response - parse error message from body
                                    log.debug("Facebook error for UID {}: {} - body: {}", uid, response.statusCode(), body);
                                    return createErrorResponse(uid, body);
                                }
                            });
                })
                .onErrorResume(Exception.class, e -> {
                    log.debug("Error checking UID {}: {}", uid, e.getMessage());
                    return Mono.just(createErrorResponse(uid, e.getMessage()));
                });
    }

    /**
     * Create error response based on error message
     */
    private FacebookCheckLiveResponseDTO createErrorResponse(String uid, String errorMsg) {
        if (errorMsg != null && errorMsg.contains("Unsupported get request. Object with ID")) {
            return FacebookCheckLiveResponseDTO.builder()
                    .uid(uid)
                    .status(CheckStatus.UNKNOWN)
                    .error("Lỗi kiểm tra: " + errorMsg.substring(0, Math.min(50, errorMsg.length())))
                    .build();
        }

        return FacebookCheckLiveResponseDTO.builder()
                .uid(uid)
                .status(CheckStatus.FAILED)
                .build();
    }

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
}
