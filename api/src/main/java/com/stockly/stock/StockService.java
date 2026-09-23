package com.stockly.stock;

import com.stockly.common.config.CacheConfig;
import com.stockly.common.exception.NotFoundException;
import com.stockly.product.Product;
import com.stockly.product.ProductMapper;
import com.stockly.product.ProductRepository;
import com.stockly.product.dto.ProductResponse;
import com.stockly.product.dto.StockAdjustRequest;
import com.stockly.stock.dto.StockMovementResponse;
import com.stockly.stock.event.InventoryEventPublisher;
import com.stockly.stock.event.LowStockEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;
    private final StockAdjustmentHandler adjustmentHandler;
    private final InventoryEventPublisher eventPublisher;

    public StockService(
            ProductRepository productRepository,
            StockMovementRepository movementRepository,
            StockAdjustmentHandler adjustmentHandler,
            InventoryEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
        this.adjustmentHandler = adjustmentHandler;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> movementsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
        return movementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = {CacheConfig.PRODUCTS, CacheConfig.PRODUCT_LISTS}, allEntries = true)
    @Transactional
    public ProductResponse adjust(Long productId, StockAdjustRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        applyAndRecord(product, MovementType.ADJUSTMENT, request.quantityDelta(),
                request.reason() == null ? "MANUAL" : request.reason(), false);
        product.setUpdatedAt(Instant.now());
        return ProductMapper.toResponse(product);
    }

    public void applyAndRecord(Product product, MovementType type, int quantity, String reference, boolean emitLowStock) {
        int delta = adjustmentHandler.apply(product, type, quantity);
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantityDelta(delta);
        movement.setQuantityAfter(product.getStockQuantity());
        movement.setReference(reference);
        movementRepository.save(movement);
        if (emitLowStock && product.isLowStock()) {
            eventPublisher.publishLowStock(new LowStockEvent(
                    product.getId(), product.getSku(), product.getStockQuantity(), product.getReorderLevel()));
        }
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getMovementType(),
                movement.getQuantityDelta(),
                movement.getQuantityAfter(),
                movement.getReference(),
                movement.getCreatedAt());
    }
}
