package com.example.packageaggregator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new package. Product IDs refer to the external product catalog.")
public class CreatePackageRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Package name", example = "Starter Pack", required = true)
    private String name;

    @Schema(description = "Optional package description", example = "Basic bundle")
    private String description;

    @NotNull(message = "Product IDs are required")
    @NotEmpty(message = "At least one product is required")
    @Schema(description = "List of external product IDs to include (from GET /products)", example = "[\"VqKb4tyj9V6i\", \"abc123\"]", required = true)
    private List<String> productIds;
}
