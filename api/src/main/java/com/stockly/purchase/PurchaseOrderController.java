package com.stockly.purchase;

import com.stockly.auth.UserAccount;
import com.stockly.purchase.dto.PurchaseOrderRequest;
import com.stockly.purchase.dto.PurchaseOrderResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public List<PurchaseOrderResponse> list() {
        return purchaseOrderService.list();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return purchaseOrderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(
            @Valid @RequestBody PurchaseOrderRequest request,
            @AuthenticationPrincipal UserAccount user) {
        return purchaseOrderService.create(request, user);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receive(@PathVariable Long id) {
        return purchaseOrderService.receive(id);
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderResponse cancel(@PathVariable Long id) {
        return purchaseOrderService.cancel(id);
    }
}
