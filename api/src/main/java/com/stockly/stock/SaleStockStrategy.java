package com.stockly.stock;

import com.stockly.common.exception.BusinessException;
import com.stockly.product.Product;
import org.springframework.stereotype.Component;

@Component
public class SaleStockStrategy implements StockAdjustmentStrategy {

    @Override
    public MovementType supports() {
        return MovementType.SALE;
    }

    @Override
    public int apply(Product product, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Sale quantity must be positive");
        }
        if (product.getStockQuantity() < quantity) {
            throw new BusinessException("Insufficient stock for SKU " + product.getSku());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return -quantity;
    }
}
