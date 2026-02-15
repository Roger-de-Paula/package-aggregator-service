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

@Component
@Slf4j
public class ProductClient {

    private static final int MAX_RETRIES = 2;

    private final WebClient productWebClient;

    public ProductClient(@Qualifier("productWebClient") WebClient productWebClient) {
        this.productWebClient = productWebClient;
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
