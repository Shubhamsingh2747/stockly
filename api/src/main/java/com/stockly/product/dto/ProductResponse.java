package com.stockly.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal unitPrice,
        int stockQuantity,
        int reorderLevel,
        boolean lowStock) {
}
