# Event-Driven Architecture Implementation Guide

## Quick Summary

Your project needs to separate **Business Events** from **Audit Events**:

### ✅ Business Events (Critical)
- `OrderConfirmedEvent` - must be guaranteed delivery
- `ProductStockUpdatedEvent` - drives saga orchestration
- **Flow:** Aggregate → Outbox → Kafka → Saga

### ✅ Audit Events (Fire-and-Forget)
- `AuditLogCreatedEvent` - for compliance/observability
- **Flow:** Published with business event → Kafka → Async persistence

---

## Architecture Flow

```
1. REQUEST HANDLER
   POST /api/orders/{id}/confirm
   ├─ Load Order aggregate
   ├─ order.confirm()
   ├─ order.registerEvent(OrderConfirmedEvent)
   └─ auditService.log(...)  → creates AuditLogCreatedEvent

2. APPLICATION LAYER (SAME @Transactional)
   ├─ repository.save(order)
   ├─ outboxService.publishEventsToOutbox(events)
   │  ├─ OrderConfirmedEvent → Outbox
   │  └─ AuditLogCreatedEvent → Outbox
   └─ order.clearEvents()

3. DATABASE (ATOMIC)
   outbox_events table:
   ├─ OrderConfirmedEvent (processed=false)
   └─ AuditLogCreatedEvent (processed=false)

4. OUTBOX POLLER (Every 1 second)
   ├─ Find unprocessed events
   ├─ Publish to Kafka
   │  ├─ topic: qlbh.order.confirmed
   │  └─ topic: qlbh.audit.log.created
   └─ Mark as processed

5. KAFKA (Message Bus)
   ├─ OrderConfirmedSaga consumes qlbh.order.confirmed
   │  ├─ Calls StockReservationService
   │  ├─ Generates ProductStockUpdatedEvent
   │  ├─ Publishes to qlbh.product.stock.updated
   │  └─ If fail → Compensate (rollback)
   │
   └─ AuditLogKafkaConsumer consumes qlbh.audit.log.created
      ├─ Persists to AuditLog table
      ├─ Sends alerts if needed
      └─ Forwards to ELK/Datadog
```

---

## Key Files & Their Roles

### 1. Domain Layer (Clean Business Logic)

#### `Order.java` (extends AggregateRoot)
```java
public void confirm() {
  validateStatus(OrderStatus.DRAFT, "...");
  if (items.isEmpty()) throw new BusinessException("...");
  
  this.status = OrderStatus.CONFIRMED;
  
  // REGISTER domain event (in memory only)
  registerEvent(new OrderConfirmedEvent(
      this.id,
      this.customerId,
      this.items,
      this.totalAmount
  ));
}
```

**Key:** Aggregate registers event but doesn't publish it.

#### `OrderConfirmedEvent.java`
```java
public class OrderConfirmedEvent implements DomainEvent {
  String orderId;
  String customerId;
  List<OrderItemSnapshot> items;
  Instant occurredAt;
}
```

**Key:** Pure data transfer object, no side effects.

### 2. Infrastructure Layer (Persistence & Pub/Sub)

#### `OutboxEntity.java`
```
Table: outbox_events
┌─────────────────────────────────────────────────────┐
│ id | eventType | payload | aggregateId | processed │
├─────────────────────────────────────────────────────┤
│ uuid-1 | OrderConfirmedEvent | {...JSON...} | order-123 | false │
│ uuid-2 | AuditLogCreatedEvent | {...JSON...} | audit-456 | false │
└─────────────────────────────────────────────────────┘
```

**Key:** Events stored WITH business transaction (ACID).

#### `OutboxService.java`
```java
@Transactional
public void publishEventsToOutbox(
    List<DomainEvent> events,
    String aggregateId,
    String aggregateType
) {
  for (DomainEvent event : events) {
    String eventType = event.getClass().getSimpleName();
    String payload = objectMapper.writeValueAsString(event);
    
    OutboxEntity entity = new OutboxEntity(
        eventType, payload, aggregateId, aggregateType
    );
    outboxRepository.save(entity);
  }
}
```

**Key:** Called within same @Transactional block as aggregate save.

#### `OutboxPoller.java`
```java
@Scheduled(fixedDelay = 1000)
@Transactional
public void processOutbox() {
  List<OutboxEntity> events = outboxRepository.findUnprocessed();
  
  for (OutboxEntity event : events) {
    try {
      publishToKafka(event);
      event.markProcessed();
      outboxRepository.save(event);
    } catch (Exception e) {
      // Retry logic here
    }
  }
}
```

**Key:** Decouples DB transaction from Kafka publish (eventual consistency).

### 3. Saga Pattern (Orchestration)

#### `OrderConfirmedSaga.java`
```java
@KafkaListener(topics = "qlbh.order.confirmed")
@Transactional
public void handleOrderConfirmed(String payload) {
  OrderConfirmedEvent event = parseEvent(payload);
  
  try {
    // Step 1: Reserve stock
    for (OrderItemSnapshot item : event.getItems()) {
      ProductStockUpdatedEvent stockEvent = 
          stockReservationService.reserve(
              item.getProductId(),
              item.getQuantity(),
              event.getOrderId()
          );
      
      // Step 2: Publish new event to Outbox
      outboxService.publishEventToOutbox(
          stockEvent,
          item.getProductId(),
          "Product"
      );
    }
  } catch (Exception e) {
    log.error("Saga failed, compensating...");
    compensate(payload);  // Rollback: release stock
    throw e;
  }
}
```

**Key:** 
- Listens to business events on Kafka
- Performs multi-step transactions
- Publishes new events on success
- Compensates on failure

### 4. Audit Processing (Async)

#### `AuditLogKafkaConsumer.java`
```java
@KafkaListener(topics = "qlbh.audit.log.created")
@Transactional
public void consumeAuditLog(String payload) {
  AuditLogCreatedEvent event = objectMapper.readValue(payload, ...);
  AuditLog auditLog = event.getAuditLog();
  
  auditRepository.save(auditLog);  // Persist
  
  // TODO: Send alerts (security)
  // TODO: Forward to ELK/Datadog
}
```

**Key:** Fire-and-forget processing (doesn't affect order flow).

---

## Application Layer Use Case Example

```java
@Transactional
public OrderResponse confirmOrder(String orderId, String userId) {
  // 1. Load aggregate
  Order order = orderRepository.findById(orderId);
  
  // 2. Execute business logic (generates OrderConfirmedEvent)
  order.confirm();
  
  // 3. Save aggregate
  orderRepository.save(order);
  
  // 4. Create audit log
  auditService.log(
      AuditAction.ORDER_CONFIRM,
      orderId,
      userId,
      true
  );  // This creates AuditLogCreatedEvent
  
  // 5. Publish ALL events to Outbox (SAME TRANSACTION)
  outboxService.publishEventsToOutbox(
      order.getEvents(),
      orderId,
      "Order"
  );
  
  // 6. Clear events (already persisted in Outbox)
  order.clearEvents();
  
  return toResponse(order);
}
```

**Atomic:** If any step fails → entire transaction rolls back.

---

## Kafka Topics Naming Convention

```
Format: qlbh.<entity>.<action>

Topics:
├── qlbh.order.confirmed      ← OrderConfirmedEvent
├── qlbh.order.cancelled      ← OrderCancelledEvent
├── qlbh.product.stock.updated ← ProductStockUpdatedEvent
├── qlbh.product.stock.released ← ProductStockReleasedEvent
└── qlbh.audit.log.created    ← AuditLogCreatedEvent
```

**Key:** Topic name auto-generated from event class name.

---

## Idempotency & Deduplication

### Problem
Kafka can deliver same message multiple times:
```
Network timeout → consumer crashes → message replayed
```

### Solution
```java
@KafkaListener(...)
public void handleOrderConfirmed(String payload) {
  OrderConfirmedEvent event = parseEvent(payload);
  String eventId = event.getEventId();
  
  if (idempotencyStore.exists(eventId)) {
    log.debug("Event already processed, skipping");
    return;  // Idempotent!
  }
  
  // Process event
  processEvent(event);
  
  // Mark as processed
  idempotencyStore.save(eventId);
}
```

**Key:** Use event ID as deduplication key in idempotency store (Redis/DB).

---

## Error Handling & Compensation

### Saga Success Path
```
OrderConfirmedEvent
  → Reserve stock
  → ProductStockUpdatedEvent
  → Success ✅
```

### Saga Failure Path (Compensation)
```
OrderConfirmedEvent
  → Reserve stock FAILS (insufficient stock)
  → LOG ERROR
  → Compensate: Release reserved stock
  → Cancel order
  → Failure ❌
```

```java
private void compensate(String orderId) {
  // Step 1: Release all reserved stock
  List<Reservation> reservations = 
      reservationService.findByOrderId(orderId);
  
  for (Reservation r : reservations) {
    stockReservationService.release(
        r.getProductId(),
        r.getQuantity(),
        orderId
    );
  }
  
  // Step 2: Cancel order
  orderService.cancel(orderId);
  
  // Step 3: Create AuditLog
  auditService.log(
      AuditAction.ORDER_CANCELLED,
      orderId,
      "SYSTEM",
      false,
      "Stock reservation failed"
  );
}
```

---

## Testing Strategy

### 1. Unit Tests (Domain)
```java
@Test
void testOrderConfirm() {
  Order order = new Order("customer-123");
  order.addItem("product-1", "Item 1", 5, Money.of(100));
  
  order.confirm();
  
  assertEquals(OrderStatus.CONFIRMED, order.getStatus());
  assertEquals(1, order.getEvents().size());
  assertThat(order.getEvents())
      .extracting(e -> e.getClass())
      .contains(OrderConfirmedEvent.class);
}
```

### 2. Integration Tests (Outbox)
```java
@Test
@Transactional
void testOrderConfirmationPublishedToOutbox() {
  Order order = orderRepository.save(new Order("customer-123"));
  order.addItem("product-1", "Item 1", 5, Money.of(100));
  
  orderRepository.save(order);
  outboxService.publishEventsToOutbox(order.getEvents(), order.getId(), "Order");
  
  List<OutboxEntity> events = outboxRepository.findUnprocessed();
  assertThat(events)
      .hasSize(1)
      .extracting(OutboxEntity::getEventType)
      .contains("OrderConfirmedEvent");
}
```

### 3. Kafka Integration Tests (Saga)
```java
@Test
void testOrderConfirmedSagaReservesStock() {
  // Publish OrderConfirmedEvent to Kafka
  kafkaTemplate.send("qlbh.order.confirmed", orderConfirmedJson);
  
  // Wait for saga to process
  Thread.sleep(2000);
  
  // Assert stock was reserved
  Product product = productRepository.findById("product-1");
  assertEquals(95, product.getStock());  // 100 - 5
}
```

---

## Summary: Correct Order of Operations

1. **Aggregate changes state** → registers event (memory)
2. **Save aggregate** (to DB)
3. **Create audit log** (if needed)
4. **Publish events to Outbox** (SAME transaction as aggregate save)
5. **Clear events** from aggregate (already persisted)
6. **Return response** to client
7. **OutboxPoller** (async, 1s later) publishes to Kafka
8. **Saga** consumes events, performs multi-step transactions
9. **Audit processor** consumes audit events, persists + alerts

---

## Common Mistakes

❌ **DON'T:** Publish events to Kafka directly from aggregate
```java
// WRONG!
order.confirm();
kafkaTemplate.send("qlbh.order.confirmed", event);  // Can lose data
```

❌ **DON'T:** Publish events without saving aggregate
```java
// WRONG!
order.confirm();
outboxService.publishEventsToOutbox(events);
// ... exception before repository.save(order)
// Event published but order not saved!
```

❌ **DON'T:** Audit event block business transaction
```java
// WRONG!
@Transactional
public void confirmOrder() {
  order.confirm();
  repository.save(order);
  
  // If this throws, entire order.confirm() rolled back!
  auditService.logToExternalSystem(order);
}
```

✅ **DO:** Use Outbox pattern for all events
✅ **DO:** Publish events same transaction as aggregate
✅ **DO:** Process audit events asynchronously (eventual consistency)
✅ **DO:** Implement compensation logic in Saga
✅ **DO:** Use event IDs for idempotency

