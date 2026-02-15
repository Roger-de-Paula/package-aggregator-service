package com.example.packageaggregator.controller;

import com.example.packageaggregator.api.dto.ProductDto;
import com.example.packageaggregator.client.ExchangeRateClient;
import com.example.packageaggregator.client.ProductClient;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal API to expose the product catalog so the frontend can let users select
 * products when building a package. All business rules (validation, snapshotting)
 * remain in the package creation flow.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final String DEFAULT_CURRENCY = "USD";

    private final ProductClient productClient;
    private final ExchangeRateClient exchangeRateClient;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts(
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        String targetCurrency = currency != null && !currency.isBlank() ? currency : DEFAULT_CURRENCY;
        BigDecimal rate = exchangeRateClient.getRateUsdTo(targetCurrency);

        List<ExternalProductResponse> external = productClient.getProducts();
        List<ProductDto> dtos = external.stream()
                .map(p -> {
                    BigDecimal priceInCurrency = p.getUsdPrice() != null
                            ? p.getUsdPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return ProductDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .price(priceInCurrency)
                            .currency(targetCurrency)
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
