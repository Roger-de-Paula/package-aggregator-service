package com.example.packageaggregator.client;

import com.example.packageaggregator.config.CacheConfig;
import com.example.packageaggregator.client.dto.ExchangeRateResponse;
import com.example.packageaggregator.exception.ExternalServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
public class ExchangeRateClient {

    private final WebClient exchangeRateWebClient;

    public ExchangeRateClient(@Qualifier("exchangeRateWebClient") WebClient exchangeRateWebClient) {
        this.exchangeRateWebClient = exchangeRateWebClient;
    }

    @Cacheable(value = CacheConfig.EXCHANGE_RATE_CACHE, cacheManager = "exchangeRateCacheManager", key = "#currency")
    public BigDecimal getRateUsdTo(String currency) {
        if ("USD".equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        }
        try {
            ExchangeRateResponse response = exchangeRateWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/latest").queryParam("from", "USD").queryParam("to", currency).build())
                    .retrieve()
                    .bodyToMono(ExchangeRateResponse.class)
                    .block();
            return Optional.ofNullable(response)
                    .map(ExchangeRateResponse::getRates)
                    .map(rates -> rates.get(currency))
                    .orElseThrow(() -> new ExternalServiceUnavailableException("No rate for currency: " + currency));
        } catch (WebClientResponseException e) {
            log.error("Exchange rate API error for {}: {}", currency, e.getStatusCode());
            throw new ExternalServiceUnavailableException("Exchange rate service unavailable: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to fetch exchange rate for {}: {}", currency, e.getMessage());
            throw new ExternalServiceUnavailableException("Exchange rate service unavailable: " + e.getMessage(), e);
        }
    }
}
