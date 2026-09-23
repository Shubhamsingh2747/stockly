package com.stockly.sales.dto;

import com.stockly.sales.SalesOrderStatus;
import java.time.Instant;
import java.util.List;

public record SalesOrderResponse(
        Long id,
        SalesOrderStatus status,
        String createdByEmail,
        Instant createdAt,
        List<SalesOrderLineResponse> lines) {
}
