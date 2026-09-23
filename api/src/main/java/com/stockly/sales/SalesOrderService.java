package com.stockly.sales;

import com.stockly.auth.UserAccount;
import com.stockly.common.config.CacheConfig;
import com.stockly.common.exception.BusinessException;
import com.stockly.common.exception.NotFoundException;
import com.stockly.product.Product;
import com.stockly.product.ProductRepository;
import com.stockly.sales.dto.SalesOrderLineRequest;
import com.stockly.sales.dto.SalesOrderLineResponse;
import com.stockly.sales.dto.SalesOrderRequest;
import com.stockly.sales.dto.SalesOrderResponse;
import com.stockly.stock.MovementType;
import com.stockly.stock.StockService;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    public SalesOrderService(
            SalesOrderRepository salesOrderRepository,
            ProductRepository productRepository,
            StockService stockService) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public List<SalesOrderResponse> list() {
        return salesOrderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public SalesOrderResponse create(SalesOrderRequest request, UserAccount user) {
        SalesOrder order = new SalesOrder();
        order.setCreatedBy(user);
        order.setStatus(SalesOrderStatus.DRAFT);
        for (SalesOrderLineRequest lineRequest : request.lines()) {
            Product product = productRepository.findById(lineRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + lineRequest.productId()));
            SalesOrderLine line = new SalesOrderLine();
            line.setProduct(product);
            line.setQuantity(lineRequest.quantity());
            line.setUnitPrice(product.getUnitPrice());
            order.addLine(line);
        }
        return toResponse(salesOrderRepository.save(order));
    }

    @CacheEvict(cacheNames = {CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public SalesOrderResponse confirm(Long id) {
        SalesOrder order = getEntity(id);
        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new BusinessException("Only DRAFT sales orders can be confirmed");
        }
        for (SalesOrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            stockService.applyAndRecord(
                    product,
                    MovementType.SALE,
                    line.getQuantity(),
                    "SO-" + order.getId(),
                    true);
        }
        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    @CacheEvict(cacheNames = {CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public SalesOrderResponse cancel(Long id) {
        SalesOrder order = getEntity(id);
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        if (order.getStatus() == SalesOrderStatus.CONFIRMED) {
            for (SalesOrderLine line : order.getLines()) {
                Product product = productRepository.findById(line.getProduct().getId())
                        .orElseThrow(() -> new NotFoundException("Product not found"));
                stockService.applyAndRecord(
                        product,
                        MovementType.ADJUSTMENT,
                        line.getQuantity(),
                        "SO-CANCEL-" + order.getId(),
                        false);
            }
        }
        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    private SalesOrder getEntity(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales order not found: " + id));
    }

    private SalesOrderResponse toResponse(SalesOrder order) {
        List<SalesOrderLineResponse> lines = order.getLines().stream()
                .map(line -> new SalesOrderLineResponse(
                        line.getProduct().getId(),
                        line.getProduct().getSku(),
                        line.getQuantity(),
                        line.getUnitPrice()))
                .toList();
        return new SalesOrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCreatedBy().getEmail(),
                order.getCreatedAt(),
                lines);
    }
}
