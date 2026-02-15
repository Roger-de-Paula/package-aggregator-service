package com.example.packageaggregator.api.mapper;

import com.example.packageaggregator.api.dto.PageDto;
import com.example.packageaggregator.api.dto.PackageProductDto;
import com.example.packageaggregator.api.dto.PackageResponseDto;
import com.example.packageaggregator.api.dto.PackageSummaryDto;
import com.example.packageaggregator.domain.entity.PackageEntity;
import com.example.packageaggregator.domain.entity.PackageProductEntity;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class PackageMapper {

    private PackageMapper() {
    }

    public static PackageSummaryDto toSummaryDto(PackageEntity entity, BigDecimal totalPriceInCurrency, String currency) {
        return PackageSummaryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .totalPrice(totalPriceInCurrency)
                .currency(currency)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static PackageResponseDto toResponseDto(PackageEntity entity, BigDecimal totalPriceInCurrency, String currency) {
        return PackageResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .totalPrice(totalPriceInCurrency)
                .currency(currency)
                .createdAt(entity.getCreatedAt())
                .products(toProductDtos(entity.getProducts()))
                .build();
    }

    public static List<PackageProductDto> toProductDtos(List<PackageProductEntity> products) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .map(PackageMapper::toProductDto)
                .collect(Collectors.toList());
    }

    public static PackageProductDto toProductDto(PackageProductEntity entity) {
        return PackageProductDto.builder()
                .externalProductId(entity.getExternalProductId())
                .productName(entity.getProductName())
                .productPriceUsd(entity.getProductPriceUsd())
                .build();
    }

    public static PageDto<PackageSummaryDto> toPageDto(Page<PackageEntity> page, List<PackageSummaryDto> content) {
        return PageDto.<PackageSummaryDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
