package com.example.packageaggregator.controller;

import com.example.packageaggregator.api.dto.CurrencyOptionDto;
import com.example.packageaggregator.client.ExchangeRateClient;
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

/**
 * Exposes supported currencies (from Frankfurter) for the frontend.
 * Search is done server-side to avoid sending the full list every time and to keep logic in one place.
 */
@RestController
@RequestMapping("/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final ExchangeRateClient exchangeRateClient;

    @GetMapping
    public ResponseEntity<List<CurrencyOptionDto>> getCurrencies(
            @RequestParam(required = false) String search) {
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
