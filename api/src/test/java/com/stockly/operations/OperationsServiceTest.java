package com.stockly.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.stockly.category.Category;
import com.stockly.product.Product;
import com.stockly.product.ProductRepository;
import com.stockly.purchase.PurchaseOrderRepository;
import com.stockly.purchase.PurchaseOrderStatus;
import com.stockly.sales.SalesOrderRepository;
import com.stockly.sales.SalesOrderStatus;
import com.stockly.stock.MovementType;
import com.stockly.stock.StockMovement;
import com.stockly.stock.StockMovementRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationsServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private OperationsService operationsService;

    @Test
    void snapshotAggregatesCountsProductsAndMovements() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Parts");

        Product widget = new Product();
        widget.setId(9L);
        widget.setSku("WIDGET");
        widget.setName("Widget");
        widget.setCategory(category);
        widget.setUnitPrice(new BigDecimal("12.50"));
        widget.setStockQuantity(2);
        widget.setReorderLevel(5);

        StockMovement movement = new StockMovement();
        movement.setId(4L);
        movement.setProduct(widget);
        movement.setMovementType(MovementType.SALE);
        movement.setQuantityDelta(-1);
        movement.setQuantityAfter(2);
        movement.setReference("SO-1");
        movement.setCreatedAt(Instant.parse("2026-09-27T10:00:00Z"));

        when(productRepository.findLowStock()).thenReturn(List.of(widget));
        when(productRepository.countLowStock()).thenReturn(1L);
        when(salesOrderRepository.countByStatus(SalesOrderStatus.DRAFT)).thenReturn(3L);
        when(purchaseOrderRepository.countByStatus(PurchaseOrderStatus.DRAFT)).thenReturn(2L);
        when(movementRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(movement));

        var snapshot = operationsService.snapshot();

        assertThat(snapshot.lowStockCount()).isEqualTo(1);
        assertThat(snapshot.openSalesDrafts()).isEqualTo(3);
        assertThat(snapshot.openPurchaseDrafts()).isEqualTo(2);
        assertThat(snapshot.lowStockProducts()).hasSize(1);
        assertThat(snapshot.lowStockProducts().get(0).sku()).isEqualTo("WIDGET");
        assertThat(snapshot.recentMovements()).hasSize(1);
        assertThat(snapshot.recentMovements().get(0).sku()).isEqualTo("WIDGET");
        assertThat(snapshot.recentMovements().get(0).movementType()).isEqualTo(MovementType.SALE);
    }
}
