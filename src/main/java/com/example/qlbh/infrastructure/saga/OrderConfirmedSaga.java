package com.example.qlbh.infrastructure.saga;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbh.domain.order.event.OrderConfirmedEvent;
import com.example.qlbh.domain.product.event.ProductStockUpdatedEvent;
import com.example.qlbh.infrastructure.outbox.OutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Saga: OrderConfirmedSaga
 *
 * Listens to: qlbh.order.confirmed
 * Performs: Reserve stock for order items
 * Publishes: ProductStockUpdatedEvent (to Outbox)
 *
 * If stock reservation fails → Compensate (release reserved stock, cancel
 * order)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedSaga {

  private final OutboxService outboxService;
  private final StockReservationService stockReservationService;

  @KafkaListener(topics = "qlbh.order.confirmed", groupId = "order-saga-group", containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void handleOrderConfirmed(String payload) {
    log.info("OrderConfirmedSaga received: {}", payload);

    try {
      // Parse event (you'd use ObjectMapper in real code)
      OrderConfirmedEvent event = parseEvent(payload);

      // Step 1: Reserve stock for each item
      for (OrderConfirmedEvent.OrderItemSnapshot item : event.getItems()) {
        reserveStock(event.getOrderId(), item);
      }

      log.info("Stock reserved for order: orderId={}", event.getOrderId());

    } catch (Exception e) {
      log.error("Saga failed for orderId={}, compensating...", extractOrderId(payload), e);
      // Compensate: cancel order, release reserved stock
      compensate(payload);
      throw e;
    }
  }

  @Transactional
  private void reserveStock(
      String orderId,
      OrderConfirmedEvent.OrderItemSnapshot item) {
    // Call stock service
    ProductStockUpdatedEvent stockEvent = stockReservationService.reserve(
        item.getProductId(),
        item.getQuantity(),
        orderId);

    // Publish stock event to Outbox (same transaction)
    outboxService.publishEventToOutbox(
        stockEvent,
        item.getProductId(),
        "Product");

    log.debug("Stock reserved: productId={}, quantity={}",
        item.getProductId(), item.getQuantity());
  }

  private void compensate(String payload) {
    // Release reserved stock, cancel order, etc.
    // This is the rollback transaction
    log.warn("Compensating saga: releasing reserved stock");
    // TODO: implement compensation logic
  }

  private OrderConfirmedEvent parseEvent(String payload) {
    // TODO: Use ObjectMapper to deserialize
    return null;
  }

  private String extractOrderId(String payload) {
    // TODO: Extract orderId from payload
    return "unknown";
  }
}
