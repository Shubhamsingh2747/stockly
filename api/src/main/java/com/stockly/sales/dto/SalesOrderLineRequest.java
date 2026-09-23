package com.stockly.sales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SalesOrderLineRequest(@NotNull Long productId, @Min(1) int quantity) {
}
