package com.stockly.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 255) String name,
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @Min(0) int stockQuantity,
        @Min(0) int reorderLevel) {
}
