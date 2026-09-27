package com.stockly.operations;

import com.stockly.operations.dto.OperationsSnapshotResponse;
import com.stockly.operations.dto.RecentMovementResponse;
import com.stockly.product.ProductMapper;
import com.stockly.product.ProductRepository;
import com.stockly.purchase.PurchaseOrderRepository;
import com.stockly.purchase.PurchaseOrderStatus;
import com.stockly.sales.SalesOrderRepository;
import com.stockly.sales.SalesOrderStatus;
import com.stockly.stock.StockMovement;
import com.stockly.stock.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationsService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockMovementRepository movementRepository;

    public OperationsService(
            ProductRepository productRepository,
            SalesOrderRepository salesOrderRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockMovementRepository movementRepository) {
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public OperationsSnapshotResponse snapshot() {
        var lowStock = productRepository.findLowStock().stream().map(ProductMapper::toResponse).toList();
        var movements = movementRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(OperationsService::toRecent)
                .toList();
        return new OperationsSnapshotResponse(
                productRepository.countLowStock(),
                salesOrderRepository.countByStatus(SalesOrderStatus.DRAFT),
                purchaseOrderRepository.countByStatus(PurchaseOrderStatus.DRAFT),
                lowStock,
                movements);
    }

    private static RecentMovementResponse toRecent(StockMovement movement) {
        var product = movement.getProduct();
        return new RecentMovementResponse(
                movement.getId(),
                product.getId(),
                product.getSku(),
                product.getName(),
                movement.getMovementType(),
                movement.getQuantityDelta(),
                movement.getQuantityAfter(),
                movement.getReference(),
                movement.getCreatedAt());
    }
}
