package com.stockly.operations;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(
        properties = {
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.open-in-view=false"
        })
class OperationsQueriesTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private StockMovementRepository movementRepository;
    @Autowired
    private SalesOrderRepository salesOrderRepository;
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Test
    void lowStockAndRecentMovementQueriesExecute() {
        Category category = new Category();
        category.setName("Parts");
        entityManager.persist(category);

        Product product = new Product();
        product.setSku("WIDGET");
        product.setName("Widget");
        product.setCategory(category);
        product.setUnitPrice(new BigDecimal("10.00"));
        product.setStockQuantity(1);
        product.setReorderLevel(5);
        entityManager.persist(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(MovementType.SALE);
        movement.setQuantityDelta(-1);
        movement.setQuantityAfter(1);
        movement.setReference("SO-1");
        entityManager.persist(movement);
        entityManager.flush();
        entityManager.clear();

        var lowStock = productRepository.findLowStock();
        assertThat(lowStock).extracting(Product::getSku).containsExactly("WIDGET");
        assertThat(lowStock.get(0).getCategory().getName()).isEqualTo("Parts");

        var recent = movementRepository.findTop20ByOrderByCreatedAtDesc();
        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getProduct().getSku()).isEqualTo("WIDGET");

        assertThat(salesOrderRepository.countByStatus(SalesOrderStatus.DRAFT)).isZero();
        assertThat(purchaseOrderRepository.countByStatus(PurchaseOrderStatus.DRAFT)).isZero();
    }
}
