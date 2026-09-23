package com.stockly.stock.dto;

import com.stockly.stock.MovementType;
import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Long productId,
        MovementType movementType,
        int quantityDelta,
        int quantityAfter,
        String reference,
        Instant createdAt) {
}
