package com.stockly.stock;

import com.stockly.product.Product;

public interface StockAdjustmentStrategy {

    MovementType supports();

    /**
     * Mutates product stock and returns the signed quantity delta applied.
     */
    int apply(Product product, int quantity);
}
