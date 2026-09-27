package com.stockly.operations.dto;

import com.stockly.product.dto.ProductResponse;
import java.util.List;

public record OperationsSnapshotResponse(
        long lowStockCount,
        long openSalesDrafts,
        long openPurchaseDrafts,
        List<ProductResponse> lowStockProducts,
        List<RecentMovementResponse> recentMovements) {
}
