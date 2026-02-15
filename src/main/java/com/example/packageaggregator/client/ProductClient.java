package com.example.packageaggregator.client;

import com.example.packageaggregator.config.CacheConfig;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import com.example.packageaggregator.exception.ExternalServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductClient {

    private static final int MAX_RETRIES = 2;

    private final WebClient productWebClient;

    public ProductClient(@Qualifier("productWebClient") WebClient productWebClient) {
        this.productWebClient = productWebClient;
    }


    /**
     * Fetches multiple products by id in parallel. Uses the same cache as {@link #getProductById(String)},
     * so repeated or duplicate ids are served from cache. Reduces wall-clock time vs sequential calls.
     * Any HTTP/timeout/5xx failure from the external API is translated to {@link ExternalServiceUnavailableException}
     * so the API returns 503 (dependency unavailable), not 500.
     */
    public Map<String, ExternalProductResponse> getProductsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        try {
            List<CompletableFuture<Map.Entry<String, ExternalProductResponse>>> futures = ids.stream()
                    .distinct()
                    .map(id -> CompletableFuture.supplyAsync(() -> Map.entry(id, getProductById(id))))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
        } catch (Exception e) {
            Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
            if (cause instanceof ExternalServiceUnavailableException) {
                throw (ExternalServiceUnavailableException) cause;
            }
            log.error("Product service unavailable during batch fetch: {}", cause != null ? cause.getMessage() : e.getMessage());
            throw new ExternalServiceUnavailableException("The product service is temporarily unavailable.", cause != null ? cause : e);
        }
    }

    @Cacheable(CacheConfig.PRODUCT_CACHE)
    public ExternalProductResponse getProductById(String id) {
        try {
            return productWebClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatus::is5xxServerError, resp -> resp.bodyToMono(String.class)
                            .map(body -> new ExternalServiceUnavailableException("Product service error: " + body)))
                    .bodyToMono(ExternalProductResponse.class)
                    .retryWhen(Retry.fixedDelay(MAX_RETRIES, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(s -> log.warn("Retrying product fetch for id {} after failure", id)))
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch product id {}: {}", id, e.getMessage());
            if (e instanceof ExternalServiceUnavailableException) {
                throw (ExternalServiceUnavailableException) e;
            }
            throw new ExternalServiceUnavailableException("Product service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all products from the external catalog for display/selection (e.g. when building a package).
     * Cached to reduce load on the external API.
     */
    @Cacheable(value = CacheConfig.PRODUCT_CACHE, key = "'list'")
    public List<ExternalProductResponse> getProducts() {
        try {
            return productWebClient.get()
                    .uri("/products")
                    .retrieve()
                    .onStatus(HttpStatus::is5xxServerError, resp -> resp.bodyToMono(String.class)
                            .map(body -> new ExternalServiceUnavailableException("Product service error: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<List<ExternalProductResponse>>() {})
                    .retryWhen(Retry.fixedDelay(MAX_RETRIES, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(s -> log.warn("Retrying product list fetch after failure")))
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch product list: {}", e.getMessage());
            if (e instanceof ExternalServiceUnavailableException) {
                throw (ExternalServiceUnavailableException) e;
            }
            throw new ExternalServiceUnavailableException("Product service unavailable: " + e.getMessage(), e);
        }
    }
}
