package com.stockly.product;

import com.stockly.product.dto.ProductResponse;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getReorderLevel(),
                product.isLowStock());
    }
}
