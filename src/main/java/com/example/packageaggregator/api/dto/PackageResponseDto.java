package com.example.packageaggregator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Full package details with product line items. Total and product prices are in the requested currency.")
public class PackageResponseDto {

    @Schema(description = "Package UUID")
    private UUID id;
    @Schema(description = "Package name")
    private String name;
    @Schema(description = "Optional description")
    private String description;
    @Schema(description = "Total price in the response currency")
    private BigDecimal totalPrice;
    @Schema(description = "Currency code (e.g. USD, EUR)")
    private String currency;
    @Schema(description = "Creation timestamp (ISO-8601)")
    private Instant createdAt;
    @Schema(description = "Snapshot of products in this package")
    private List<PackageProductDto> products;
}
