package com.stockly.sales.dto;

import java.math.BigDecimal;

public record SalesOrderLineResponse(Long productId, String sku, int quantity, BigDecimal unitPrice) {
}
