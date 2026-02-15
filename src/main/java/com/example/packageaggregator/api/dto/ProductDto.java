package com.example.packageaggregator.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO for exposing product catalog to the frontend (e.g. for building a package by selection).
 * price and currency reflect the requested display currency (from query param).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private String id;
    private String name;
    /** Price in the requested currency (see currency). */
    private BigDecimal price;
    /** Display currency (e.g. USD, EUR). */
    private String currency;
}
