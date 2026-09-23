package com.stockly.purchase;

import com.stockly.auth.UserAccount;
import com.stockly.common.config.CacheConfig;
import com.stockly.common.exception.BusinessException;
import com.stockly.common.exception.NotFoundException;
import com.stockly.product.Product;
import com.stockly.product.ProductRepository;
import com.stockly.purchase.dto.PurchaseOrderLineRequest;
import com.stockly.purchase.dto.PurchaseOrderLineResponse;
import com.stockly.purchase.dto.PurchaseOrderRequest;
import com.stockly.purchase.dto.PurchaseOrderResponse;
import com.stockly.stock.MovementType;
import com.stockly.stock.StockService;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            ProductRepository productRepository,
            StockService stockService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> list() {
        return purchaseOrderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request, UserAccount user) {
        PurchaseOrder order = new PurchaseOrder();
        order.setCreatedBy(user);
        order.setStatus(PurchaseOrderStatus.DRAFT);
        for (PurchaseOrderLineRequest lineRequest : request.lines()) {
            Product product = productRepository.findById(lineRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + lineRequest.productId()));
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProduct(product);
            line.setQuantity(lineRequest.quantity());
            line.setUnitCost(lineRequest.unitCost());
            order.addLine(line);
        }
        return toResponse(purchaseOrderRepository.save(order));
    }

    @CacheEvict(cacheNames = {CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public PurchaseOrderResponse receive(Long id) {
        PurchaseOrder order = getEntity(id);
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessException("Only DRAFT purchase orders can be received");
        }
        for (PurchaseOrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            stockService.applyAndRecord(
                    product,
                    MovementType.PURCHASE,
                    line.getQuantity(),
                    "PO-" + order.getId(),
                    false);
        }
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder order = getEntity(id);
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessException("Received purchase orders cannot be cancelled");
        }
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    private PurchaseOrder getEntity(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found: " + id));
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        List<PurchaseOrderLineResponse> lines = order.getLines().stream()
                .map(line -> new PurchaseOrderLineResponse(
                        line.getProduct().getId(),
                        line.getProduct().getSku(),
                        line.getQuantity(),
                        line.getUnitCost()))
                .toList();
        return new PurchaseOrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCreatedBy().getEmail(),
                order.getCreatedAt(),
                lines);
    }
}
