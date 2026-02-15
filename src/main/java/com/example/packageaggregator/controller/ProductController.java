package com.example.packageaggregator.controller;

import com.example.packageaggregator.api.dto.ProductDto;
import com.example.packageaggregator.client.ExchangeRateClient;
import com.example.packageaggregator.client.ProductClient;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products (internal)", description = "Product catalog for the frontend. Used when building a package by selection. Prices are returned in the requested currency. Cached.")
public class ProductController {

    private static final String DEFAULT_CURRENCY = "USD";

    private final ProductClient productClient;
    private final ExchangeRateClient exchangeRateClient;

    @Operation(summary = "List all products", description = "Returns the full product catalog from the external API with prices converted to the requested currency. Used by the frontend 'Create Package' flow.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of products with price and currency", content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "503", description = "Product or exchange rate service unavailable")
    })
    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts(
            @Parameter(description = "Target currency for prices (e.g. USD, EUR)") @RequestParam(required = false, defaultValue = "USD") String currency) {
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
