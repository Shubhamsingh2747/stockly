package com.stockly.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PurchaseOrderRequest(@NotEmpty @Valid List<PurchaseOrderLineRequest> lines) {
}
