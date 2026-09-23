package com.stockly.stock.event;

/**
 * Port for inventory notifications.
 * TODO: add KafkaInventoryEventPublisher writing to topic {@code inventory.events}
 * without changing callers of this interface.
 */
public interface InventoryEventPublisher {

    void publishLowStock(LowStockEvent event);
}
