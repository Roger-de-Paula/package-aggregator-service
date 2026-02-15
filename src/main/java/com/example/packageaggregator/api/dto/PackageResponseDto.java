package com.example.packageaggregator.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageResponseDto {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal totalPrice;
    private String currency;
    private Instant createdAt;
    private List<PackageProductDto> products;
}
