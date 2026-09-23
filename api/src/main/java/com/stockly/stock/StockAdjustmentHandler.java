package com.stockly.stock;

import com.stockly.common.exception.BusinessException;
import com.stockly.product.Product;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StockAdjustmentHandler {

    private final Map<MovementType, StockAdjustmentStrategy> strategies = new EnumMap<>(MovementType.class);

    public StockAdjustmentHandler(List<StockAdjustmentStrategy> strategyList) {
        for (StockAdjustmentStrategy strategy : strategyList) {
            strategies.put(strategy.supports(), strategy);
        }
    }

    public int apply(Product product, MovementType type, int quantity) {
        StockAdjustmentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new BusinessException("No stock strategy for " + type);
        }
        return strategy.apply(product, quantity);
    }
}
