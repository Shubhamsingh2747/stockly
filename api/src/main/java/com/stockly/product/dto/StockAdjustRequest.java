package com.stockly.product.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustRequest(@NotNull Integer quantityDelta, String reason) {
}
