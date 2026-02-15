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
import com.example.packageaggregator.exception.InvalidProductException;
import com.example.packageaggregator.exception.PackageNotFoundException;
import com.example.packageaggregator.repository.PackageJpaRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final PackageJpaRepository packageRepository;
    private final ProductClient productClient;
    private final ExchangeRateClient exchangeRateClient;

    /**
     * Fetch and validate products outside the transaction, then persist in a short DB-only transaction.
     * Avoids holding a DB connection while calling external APIs (anti-pattern).
     */
    public PackageResponseDto create(CreatePackageRequest request) {
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            throw new InvalidProductException("At least one product is required");
        }
        List<String> uniqueIds = new ArrayList<>(new LinkedHashSet<>(request.getProductIds()));
        log.info("Creating package '{}' with {} product(s)", request.getName(), uniqueIds.size());

        Map<String, ExternalProductResponse> productMap = productClient.getProductsByIds(uniqueIds);
        if (productMap.size() != uniqueIds.size()) {
            throw new InvalidProductException("One or more products do not exist or are unavailable");
        }

        List<PackageProductEntity> productEntities = new ArrayList<>();
        BigDecimal totalUsd = BigDecimal.ZERO;

        for (String productId : uniqueIds) {
            ExternalProductResponse external = productMap.get(productId);
            if (external == null || external.getUsdPrice() == null) {
                throw new InvalidProductException("Invalid or missing product data for id: " + productId);
            }
            BigDecimal price = external.getUsdPrice();
            totalUsd = totalUsd.add(price);
            productEntities.add(PackageProductEntity.builder()
                    .externalProductId(external.getId())
                    .productName(external.getName())
                    .productPriceUsd(price)
                    .build());
        }

        PackageEntity entity = persistPackage(request.getName(), request.getDescription(), totalUsd, productEntities);
        return toResponseWithCurrency(entity, DEFAULT_CURRENCY);
    }

    @Transactional
    protected PackageEntity persistPackage(String name, String description, BigDecimal totalUsd,
                                          List<PackageProductEntity> productEntities) {
        PackageEntity entity = PackageEntity.builder()
                .name(name)
                .description(description)
                .totalPriceUsd(totalUsd)
                .createdAt(Instant.now())
                .deleted(false)
                .build();

        for (PackageProductEntity pe : productEntities) {
            pe.setPackageEntity(entity);
            entity.getProducts().add(pe);
        }
        return packageRepository.save(entity);
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
        BigDecimal rate = getRateForCurrency(targetCurrency);
        Page<PackageEntity> page = packageRepository.findAllByDeletedFalse(pageable);
        List<PackageSummaryDto> content = page.getContent().stream()
                .map(entity -> {
                    BigDecimal converted = convertTotalWithRate(entity.getTotalPriceUsd(), rate);
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
        return toResponseWithCurrency(entity, DEFAULT_CURRENCY);
    }

    @Transactional
    public void softDelete(UUID id) {
        PackageEntity entity = packageRepository.findById(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
        entity.setDeleted(true);
        log.info("Soft deleted package: {}", id);
    }

    private PackageResponseDto toResponseWithCurrency(PackageEntity entity, String currency) {
        BigDecimal rate = getRateForCurrency(currency);
        BigDecimal convertedTotal = convertTotalWithRate(entity.getTotalPriceUsd(), rate);
        return PackageMapper.toResponseDto(entity, convertedTotal, currency, rate);
    }

    private BigDecimal getRateForCurrency(String currency) {
        if (DEFAULT_CURRENCY.equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        }
        return exchangeRateClient.getRateUsdTo(currency);
    }

    private BigDecimal convertTotal(BigDecimal totalUsd, String currency) {
        if (totalUsd == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = getRateForCurrency(currency);
        return convertTotalWithRate(totalUsd, rate);
    }

    private BigDecimal convertTotalWithRate(BigDecimal totalUsd, BigDecimal rate) {
        if (totalUsd == null) {
            return BigDecimal.ZERO;
        }
        return totalUsd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
