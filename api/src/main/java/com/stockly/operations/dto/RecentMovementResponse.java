package com.stockly.operations.dto;

import com.stockly.stock.MovementType;
import java.time.Instant;

public record RecentMovementResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        MovementType movementType,
        int quantityDelta,
        int quantityAfter,
        String reference,
        Instant createdAt) {
}
