package com.stockly.stock;

import com.stockly.common.exception.BusinessException;
import com.stockly.product.Product;
import org.springframework.stereotype.Component;

@Component
public class AdjustmentStockStrategy implements StockAdjustmentStrategy {

    @Override
    public MovementType supports() {
        return MovementType.ADJUSTMENT;
    }

    @Override
    public int apply(Product product, int quantity) {
        if (quantity == 0) {
            throw new BusinessException("Adjustment quantity cannot be zero");
        }
        int next = product.getStockQuantity() + quantity;
        if (next < 0) {
            throw new BusinessException("Adjustment would make stock negative for SKU " + product.getSku());
        }
        product.setStockQuantity(next);
        return quantity;
    }
}
