package com.example.packageaggregator.controller;

import com.example.packageaggregator.api.dto.CreatePackageRequest;
import com.example.packageaggregator.api.dto.PackageResponseDto;
import com.example.packageaggregator.api.dto.PackageSummaryDto;
import com.example.packageaggregator.api.dto.PageDto;
import com.example.packageaggregator.api.dto.UpdatePackageRequest;
import com.example.packageaggregator.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Packages", description = "Create, read, update, and soft-delete packages. Totals and product prices are converted to the requested currency at response time.")
public class PackageController {

    private final PackageService packageService;

    @Operation(operationId = "createPackage", summary = "Create a package", description = "Creates a new package with the given name, description, and product IDs. Products are fetched from the external API and snapshotted (name, price in USD). Total is stored in USD.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Package created", content = @Content(schema = @Schema(implementation = PackageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. invalid or missing product IDs)"),
            @ApiResponse(responseCode = "503", description = "External product service unavailable")
    })
    @PostMapping
    public ResponseEntity<PackageResponseDto> create(@Valid @RequestBody CreatePackageRequest request) {
        PackageResponseDto created = packageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(operationId = "getPackageById", summary = "Get package by ID", description = "Returns a single package with products. Total and each product price are converted to the requested currency.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package found", content = @Content(schema = @Schema(implementation = PackageResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Package not found"),
            @ApiResponse(responseCode = "503", description = "Exchange rate service unavailable for the requested currency")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PackageResponseDto> getById(
            @Parameter(description = "Package UUID") @PathVariable UUID id,
            @Parameter(description = "Target currency for amounts (e.g. USD, EUR, GBP)") @RequestParam(required = false, defaultValue = "USD") String currency) {
        PackageResponseDto dto = packageService.getById(id, currency);
        return ResponseEntity.ok(dto);
    }

    @Operation(operationId = "getPackages", summary = "List packages (paginated)", description = "Returns a page of packages sorted by creation date (newest first). Totals are in the requested currency.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of packages", content = @Content(schema = @Schema(implementation = PageDto.class))),
            @ApiResponse(responseCode = "503", description = "Exchange rate service unavailable")
    })
    @GetMapping
    public ResponseEntity<PageDto<PackageSummaryDto>> getAll(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Target currency for totals") @RequestParam(required = false, defaultValue = "USD") String currency) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageDto<PackageSummaryDto> result = packageService.getAll(pageable, currency);
        return ResponseEntity.ok(result);
    }

    @Operation(operationId = "updatePackage", summary = "Update package", description = "Updates the package name and description only. Product list is immutable after creation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package updated", content = @Content(schema = @Schema(implementation = PackageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Package not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PackageResponseDto> update(
            @Parameter(description = "Package UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdatePackageRequest request) {
        PackageResponseDto dto = packageService.update(id, request);
        return ResponseEntity.ok(dto);
    }

    @Operation(operationId = "deletePackage", summary = "Soft-delete package", description = "Marks the package as deleted. Idempotent: deleting an already-deleted package also returns 204. Data is retained for audit.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Package deleted (or already deleted)"),
            @ApiResponse(responseCode = "404", description = "Package not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Package UUID") @PathVariable UUID id) {
        packageService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
