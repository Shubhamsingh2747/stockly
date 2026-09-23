package com.stockly.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stockly.common.exception.BusinessException;
import com.stockly.product.Product;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockAdjustmentHandlerTest {

    private StockAdjustmentHandler handler;
    private Product product;

    @BeforeEach
    void setUp() {
        handler = new StockAdjustmentHandler(List.of(
                new SaleStockStrategy(),
                new PurchaseStockStrategy(),
                new AdjustmentStockStrategy()));
        product = new Product();
        product.setSku("SKU-1");
        product.setStockQuantity(10);
    }

    @Test
    void saleDecrementsStock() {
        int delta = handler.apply(product, MovementType.SALE, 4);
        assertThat(delta).isEqualTo(-4);
        assertThat(product.getStockQuantity()).isEqualTo(6);
    }

    @Test
    void saleFailsWhenInsufficient() {
        assertThatThrownBy(() -> handler.apply(product, MovementType.SALE, 11))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void purchaseIncrementsStock() {
        handler.apply(product, MovementType.PURCHASE, 5);
        assertThat(product.getStockQuantity()).isEqualTo(15);
    }

    @Test
    void adjustmentCanDecrease() {
        handler.apply(product, MovementType.ADJUSTMENT, -3);
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }
}
