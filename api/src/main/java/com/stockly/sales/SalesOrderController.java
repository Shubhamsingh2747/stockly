package com.stockly.sales;

import com.stockly.auth.UserAccount;
import com.stockly.sales.dto.SalesOrderRequest;
import com.stockly.sales.dto.SalesOrderResponse;
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
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    public List<SalesOrderResponse> list() {
        return salesOrderService.list();
    }

    @GetMapping("/{id}")
    public SalesOrderResponse get(@PathVariable Long id) {
        return salesOrderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderResponse create(
            @Valid @RequestBody SalesOrderRequest request,
            @AuthenticationPrincipal UserAccount user) {
        return salesOrderService.create(request, user);
    }

    @PostMapping("/{id}/confirm")
    public SalesOrderResponse confirm(@PathVariable Long id) {
        return salesOrderService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public SalesOrderResponse cancel(@PathVariable Long id) {
        return salesOrderService.cancel(id);
    }
}
