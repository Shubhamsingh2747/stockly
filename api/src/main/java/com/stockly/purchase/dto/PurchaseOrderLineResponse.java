package com.stockly.purchase.dto;

import java.math.BigDecimal;

public record PurchaseOrderLineResponse(Long productId, String sku, int quantity, BigDecimal unitCost) {
}
