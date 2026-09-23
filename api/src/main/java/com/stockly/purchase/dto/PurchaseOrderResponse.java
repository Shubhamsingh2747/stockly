package com.stockly.purchase.dto;

import com.stockly.purchase.PurchaseOrderStatus;
import java.time.Instant;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        PurchaseOrderStatus status,
        String createdByEmail,
        Instant createdAt,
        List<PurchaseOrderLineResponse> lines) {
}
