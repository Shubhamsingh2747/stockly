package com.stockly.product;

import com.stockly.product.dto.ProductRequest;
import com.stockly.product.dto.ProductResponse;
import com.stockly.product.dto.StockAdjustRequest;
import com.stockly.stock.StockService;
import com.stockly.stock.dto.StockMovementResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;

    public ProductController(ProductService productService, StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }

    @GetMapping
    public List<ProductResponse> list(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Boolean lowStock) {
        return productService.list(sku, category, lowStock);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PostMapping("/{id}/adjust-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse adjust(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest request) {
        return stockService.adjust(id, request);
    }

    @GetMapping("/{id}/movements")
    public List<StockMovementResponse> movements(@PathVariable Long id) {
        return stockService.movementsForProduct(id);
    }
}
