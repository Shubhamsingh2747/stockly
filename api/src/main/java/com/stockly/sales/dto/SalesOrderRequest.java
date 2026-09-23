package com.stockly.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SalesOrderRequest(@NotEmpty @Valid List<SalesOrderLineRequest> lines) {
}
