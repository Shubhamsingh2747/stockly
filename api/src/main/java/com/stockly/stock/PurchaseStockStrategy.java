package com.stockly.stock;

import com.stockly.common.exception.BusinessException;
import com.stockly.product.Product;
import org.springframework.stereotype.Component;

@Component
public class PurchaseStockStrategy implements StockAdjustmentStrategy {

    @Override
    public MovementType supports() {
        return MovementType.PURCHASE;
    }

    @Override
    public int apply(Product product, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Purchase quantity must be positive");
        }
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return quantity;
    }
}
