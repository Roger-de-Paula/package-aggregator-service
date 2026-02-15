package com.example.packageaggregator.controller;

import com.example.packageaggregator.api.dto.CreatePackageRequest;
import com.example.packageaggregator.api.dto.PackageResponseDto;
import com.example.packageaggregator.api.dto.PackageSummaryDto;
import com.example.packageaggregator.api.dto.PageDto;
import com.example.packageaggregator.api.dto.UpdatePackageRequest;
import com.example.packageaggregator.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    @PostMapping
    public ResponseEntity<PackageResponseDto> create(@Valid @RequestBody CreatePackageRequest request) {
        PackageResponseDto created = packageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageResponseDto> getById(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        PackageResponseDto dto = packageService.getById(id, currency);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<PageDto<PackageSummaryDto>> getAll(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageDto<PackageSummaryDto> result = packageService.getAll(pageable, currency);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePackageRequest request) {
        PackageResponseDto dto = packageService.update(id, request);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        packageService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
