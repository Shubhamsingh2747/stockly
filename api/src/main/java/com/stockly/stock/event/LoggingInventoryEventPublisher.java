package com.stockly.stock.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingInventoryEventPublisher implements InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingInventoryEventPublisher.class);

    @Override
    public void publishLowStock(LowStockEvent event) {
        log.warn("Low stock event sku={} qty={} reorderLevel={}",
                event.sku(), event.stockQuantity(), event.reorderLevel());
    }
}
