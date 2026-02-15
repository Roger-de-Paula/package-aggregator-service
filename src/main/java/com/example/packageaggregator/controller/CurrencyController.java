package com.example.packageaggregator.controller;

import com.example.packageaggregator.api.dto.CurrencyOptionDto;
import com.example.packageaggregator.client.ExchangeRateClient;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/currencies")
@RequiredArgsConstructor
@Tag(name = "Currencies (internal)", description = "Supported currencies from Frankfurter. Used by the frontend currency selector. Search filters by code or name. Cached.")
public class CurrencyController {

    private final ExchangeRateClient exchangeRateClient;

    @Operation(summary = "List supported currencies", description = "Returns currencies supported for display (from Frankfurter). Optional search filters by currency code or full name (case-insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of currency options (code and name)", content = @Content(schema = @Schema(implementation = CurrencyOptionDto.class))),
            @ApiResponse(responseCode = "503", description = "Frankfurter service unavailable")
    })
    @GetMapping
    public ResponseEntity<List<CurrencyOptionDto>> getCurrencies(
            @Parameter(description = "Filter by code or name (e.g. 'ja' for JPY, 'Japanese')") @RequestParam(required = false) String search) {
        Map<String, String> all = exchangeRateClient.getCurrencies();
        String searchTrimmed = search != null ? search.trim().toLowerCase() : "";

        List<CurrencyOptionDto> list = all.entrySet().stream()
                .filter(e -> searchTrimmed.isEmpty()
                        || e.getKey().toLowerCase().contains(searchTrimmed)
                        || (e.getValue() != null && e.getValue().toLowerCase().contains(searchTrimmed)))
                .map(e -> CurrencyOptionDto.builder()
                        .code(e.getKey())
                        .name(e.getValue() != null ? e.getValue() : e.getKey())
                        .build())
                .sorted(Comparator.comparing(CurrencyOptionDto::getCode))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
