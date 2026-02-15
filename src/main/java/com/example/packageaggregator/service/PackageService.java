package com.example.packageaggregator.service;

import com.example.packageaggregator.api.dto.CreatePackageRequest;
import com.example.packageaggregator.api.dto.PackageResponseDto;
import com.example.packageaggregator.api.dto.PackageSummaryDto;
import com.example.packageaggregator.api.dto.PageDto;
import com.example.packageaggregator.api.dto.UpdatePackageRequest;
import com.example.packageaggregator.api.mapper.PackageMapper;
import com.example.packageaggregator.client.ExchangeRateClient;
import com.example.packageaggregator.client.ProductClient;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import com.example.packageaggregator.domain.entity.PackageEntity;
import com.example.packageaggregator.domain.entity.PackageProductEntity;
import com.example.packageaggregator.exception.PackageNotFoundException;
import com.example.packageaggregator.repository.PackageJpaRepository;
import com.example.packageaggregator.repository.PackageProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final PackageJpaRepository packageRepository;
    private final PackageProductJpaRepository packageProductRepository;
    private final ProductClient productClient;
    private final ExchangeRateClient exchangeRateClient;

    @Transactional
    public PackageResponseDto create(CreatePackageRequest request) {
        log.info("Creating package: {}", request.getName());

        List<PackageProductEntity> productEntities = new ArrayList<>();
        BigDecimal totalUsd = BigDecimal.ZERO;

        for (Long productId : request.getProductIds()) {
            ExternalProductResponse external = productClient.getProductById(productId);
            if (external == null || external.getUsdPrice() == null) {
                throw new IllegalArgumentException("Invalid product data for id: " + productId);
            }
            BigDecimal price = external.getUsdPrice();
            totalUsd = totalUsd.add(price);
            PackageProductEntity productEntity = PackageProductEntity.builder()
                    .id(UUID.randomUUID())
                    .externalProductId(external.getId())
                    .productName(external.getName())
                    .productPriceUsd(price)
                    .build();
            productEntities.add(productEntity);
        }

        PackageEntity entity = PackageEntity.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .totalPriceUsd(totalUsd)
                .createdAt(Instant.now())
                .deleted(false)
                .build();

        entity = packageRepository.save(entity);
        for (PackageProductEntity pe : productEntities) {
            pe.setPackageEntity(entity);
            packageProductRepository.save(pe);
        }
        entity.getProducts().addAll(productEntities);

        return toResponseWithCurrency(entity, DEFAULT_CURRENCY);
    }

    @Transactional(readOnly = true)
    public PackageResponseDto getById(UUID id, String currency) {
        String targetCurrency = currency != null && !currency.isBlank() ? currency : DEFAULT_CURRENCY;
        PackageEntity entity = packageRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
        return toResponseWithCurrency(entity, targetCurrency);
    }

    @Transactional(readOnly = true)
    public PageDto<PackageSummaryDto> getAll(Pageable pageable, String currency) {
        String targetCurrency = currency != null && !currency.isBlank() ? currency : DEFAULT_CURRENCY;
        Page<PackageEntity> page = packageRepository.findAllByDeletedFalse(pageable);
        List<PackageSummaryDto> content = page.getContent().stream()
                .map(entity -> {
                    BigDecimal converted = convertTotal(entity.getTotalPriceUsd(), targetCurrency);
                    return PackageMapper.toSummaryDto(entity, converted, targetCurrency);
                })
                .collect(Collectors.toList());
        return PackageMapper.toPageDto(page, content);
    }

    @Transactional
    public PackageResponseDto update(UUID id, UpdatePackageRequest request) {
        PackageEntity entity = packageRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity = packageRepository.save(entity);
        entity.getProducts().size();
        return toResponseWithCurrency(entity, DEFAULT_CURRENCY);
    }

    @Transactional
    public void softDelete(UUID id) {
        PackageEntity entity = packageRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
        entity.setDeleted(true);
        packageRepository.save(entity);
        log.info("Soft deleted package: {}", id);
    }

    private PackageResponseDto toResponseWithCurrency(PackageEntity entity, String currency) {
        BigDecimal convertedTotal = convertTotal(entity.getTotalPriceUsd(), currency);
        return PackageMapper.toResponseDto(entity, convertedTotal, currency);
    }

    private BigDecimal convertTotal(BigDecimal totalUsd, String currency) {
        if (totalUsd == null) {
            return BigDecimal.ZERO;
        }
        if (DEFAULT_CURRENCY.equalsIgnoreCase(currency)) {
            return totalUsd.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = exchangeRateClient.getRateUsdTo(currency);
        return totalUsd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
