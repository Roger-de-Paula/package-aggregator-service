package com.example.packageaggregator.api.dto;

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
public class CreatePackageRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Product IDs are required")
    @NotEmpty(message = "At least one product is required")
    private List<String> productIds;
}
