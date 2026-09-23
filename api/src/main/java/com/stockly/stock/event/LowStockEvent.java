package com.stockly.stock.event;

public record LowStockEvent(Long productId, String sku, int stockQuantity, int reorderLevel) {
}
