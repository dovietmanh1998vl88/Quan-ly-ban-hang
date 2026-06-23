# Implementation Checklist: Event-Driven Architecture

## Current Issues

Your code has a fundamental issue with the event flow:

```
❌ CURRENT (WRONG):
Order.confirm() → registerEvent (memory)
  ↓
repository.save(order)
  ↓
eventPublisher.publishEvent() (Spring local event)
  ↓
AuditEventListener @Async (separate thread, can fail!)
```

**Problem:** Events are in memory, not persisted. If Kafka fails, event is lost.

---

## Correct Flow (Outbox Pattern)

```
✅ CORRECT:
Order.confirm() → registerEvent (memory)
  ↓
@Transactional {
  repository.save(order)
  outboxService.publishEventsToOutbox(events)  // SAME TX
  auditService.createAuditLog(...)  // Creates AuditLogCreatedEvent
  outboxService.publishEventsToOutbox(auditEvents)  // SAME TX
}
  ↓
Outbox table persisted (ACID guarantee)
  ↓
OutboxPoller (async) → Kafka → Saga
```

---

## Changes Required

### 1. Update `OrderApplicationService.execute(ConfirmOrderCommand)`

**Before:**
```java
@Transactional
public OrderDto execute(ConfirmOrderCommand command) {
  Order order = orderRepository.findByIdForUpdate(...);
  order.confirm();
  return mapper.toDto(orderRepository.save(order));
  // ❌ Events NOT persisted, NOT published
}
```

**After:**
```java
@Transactional
public OrderDto execute(ConfirmOrderCommand command) {
  Order order = orderRepository.findByIdForUpdate(...);
  
  // Business logic
  order.confirm();  // Registers OrderConfirmedEvent
  orderRepository.save(order);
  
  // Create audit log (generates AuditLogCreatedEvent)
  auditService.createAuditLog(
      AuditLogDto.builder()
          .action(AuditAction.ORDER_CONFIRM)
          .entityId(order.getId())
          .status(AuditStatus.SUCCESS)
          .durationMs(duration)
          .build()
  );
  
  // Publish ALL events to Outbox (SAME transaction)
  outboxService.publishEventsToOutbox(
      order.getEvents(),
      order.getId(),
      "Order"
  );
  
  order.clearEvents();
  return mapper.toDto(order);
}
```

### 2. Update `AuditApplicationService`

**Current:** Uses `Propagation.REQUIRES_NEW` (separate transaction)
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void execute(AuditLogCommand command) {
  // ❌ Separate transaction from business operation
}
```

**New:** Should publish events to Outbox (SAME transaction)
```java
@Transactional
public void createAuditLog(AuditLogDto dto) {
  AuditLog auditLog = auditRepository.save(mapper.toDomain(dto));
  
  AuditLogCreatedEvent event = new AuditLogCreatedEvent(auditLog);
  
  // Caller will publish this to Outbox
  return event;
}
```

### 3. Add `OutboxService` injection to `OrderApplicationService`

```java
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
  private final OrderDomainRepository orderRepository;
  private final OutboxService outboxService;  // ✅ ADD THIS
  private final AuditApplicationService auditService;  // Already has
}
```

### 4. Fix `AuditApplicationService` pattern

**Change from:**
- Using `ApplicationEventPublisher.publishEvent()` (local, unreliable)
- Using `REQUIRES_NEW` transaction (separate from business)

**Change to:**
- Creating `AuditLogCreatedEvent` object
- Returning event to caller
- Caller publishes to Outbox (same transaction)

---

## Key Responsibilities per Layer

### Domain Layer (`Order`, `DomainEvent`)
✅ Registers events (memory only)
✅ Encodes business logic
❌ Never publishes events

### Application Layer (`OrderApplicationService`)
✅ Loads aggregate
✅ Executes business logic
✅ Saves aggregate
✅ Creates audit log
✅ **Publishes ALL events to Outbox (same transaction)**
✅ Clears events from aggregate

### Infrastructure Layer (`OutboxService`, `OutboxPoller`)
✅ Persists events to database
✅ Polls and publishes to Kafka
✅ Handles retries

### Saga Layer (`OrderConfirmedSaga`)
✅ Listens to Kafka topics
✅ Executes multi-step business logic
✅ Publishes new events (compensation)
✅ Handles rollback

### Audit Layer (`AuditApplicationService`, `AuditLogKafkaConsumer`)
✅ Creates audit log in memory
✅ Publishes AuditLogCreatedEvent to Outbox
✅ Async consumer persists audit log to database
✅ Sends alerts/metrics

---

## Testing Strategy

### Test 1: Domain Logic
```java
@Test
void testOrderConfirm() {
  Order order = new Order("customer-123");
  order.addItem("product-1", "Item 1", 5, Money.of(100));
  
  order.confirm();
  
  assertEquals(OrderStatus.CONFIRMED, order.getStatus());
  assertEquals(1, order.getEvents().size());
  assertThat(order.getEvents().get(0))
      .isInstanceOf(OrderConfirmedEvent.class);
}
```

### Test 2: Outbox Persistence
```java
@Test
@Transactional
void testEventsPublishedToOutbox() {
  Order order = orderRepository.save(new Order("customer-123"));
  order.addItem("product-1", "Item 1", 5, Money.of(100));
  
  orderApplicationService.confirmOrder(...);  // Publishes to Outbox
  
  List<OutboxEntity> events = outboxRepository.findUnprocessed();
  assertThat(events)
      .hasSize(2)  // OrderConfirmedEvent + AuditLogCreatedEvent
      .extracting(OutboxEntity::getEventType)
      .contains("OrderConfirmedEvent", "AuditLogCreatedEvent");
}
```

### Test 3: Kafka Integration
```java
@Test
void testOrderConfirmedSagaReservesStock() {
  // Publish to Kafka
  kafkaTemplate.send("qlbh.order.confirmed", orderConfirmedJson);
  
  Thread.sleep(2000);  // Wait for async processing
  
  // Assert stock was reserved
  Product product = productRepository.findById("product-1");
  assertEquals(95, product.getStock());  // 100 - 5
}
```

---

## Migration Path

### Phase 1: Add Outbox Infrastructure
- ✅ Create `OutboxEntity`
- ✅ Create `OutboxRepository`
- ✅ Create `OutboxService`
- ✅ Update `OutboxPoller`
- ✅ Create `KafkaConfig`

### Phase 2: Update Domain Events
- ✅ Fix `OrderConfirmedEvent` (with `implements DomainEvent`)
- ✅ Create `OrderCancelledEvent`
- ✅ Update `Order.confirm()` to register event

### Phase 3: Update Application Services
- ⏳ Update `OrderApplicationService.execute(ConfirmOrderCommand)`
- ⏳ Update `AuditApplicationService`
- ⏳ Remove `ApplicationEventPublisher` usage
- ⏳ Add `OutboxService` injection

### Phase 4: Add Saga Pattern
- ✅ Create `OrderConfirmedSaga`
- ✅ Create `StockReservationService`
- ✅ Handle compensation logic

### Phase 5: Add Kafka Consumers
- ✅ Create `AuditLogKafkaConsumer`
- ✅ Create Kafka topics (docker-compose)

### Phase 6: Testing & Verification
- Test database transactions
- Test Kafka message delivery
- Test saga compensation
- Load testing

---

## Code Examples Provided

These files are already created in your project:

1. ✅ `infrastructure/outbox/OutboxEntity.java` - Fixed
2. ✅ `infrastructure/outbox/OutboxRepository.java` - New
3. ✅ `infrastructure/outbox/OutboxService.java` - New
4. ✅ `infrastructure/scheduler/OutboxPoller.java` - Fixed
5. ✅ `domain/order/event/OrderConfirmedEvent.java` - Fixed
6. ✅ `domain/order/event/OrderCancelledEvent.java` - New
7. ✅ `domain/order/model/Order.java` - Fixed (extends AggregateRoot)
8. ✅ `infrastructure/saga/OrderConfirmedSaga.java` - New
9. ✅ `infrastructure/saga/StockReservationService.java` - New
10. ✅ `infrastructure/kafka/AuditLogKafkaConsumer.java` - New
11. ✅ `infrastructure/config/KafkaConfig.java` - New

---

## Documentation Provided

1. ✅ `ARCHITECTURE.md` - High-level architecture diagram
2. ✅ `EVENT_DRIVEN_GUIDE.md` - Detailed implementation guide

---

## Next Steps

1. **Read** `EVENT_DRIVEN_GUIDE.md` for complete understanding
2. **Update** `OrderApplicationService.execute(ConfirmOrderCommand)` to:
   - Call `outboxService.publishEventsToOutbox()`
   - Ensure audit logging happens SAME transaction
3. **Update** `AuditApplicationService` to return events instead of publishing directly
4. **Test** the flow end-to-end
5. **Verify** Kafka message delivery with docker-compose

---

## Docker Compose for Kafka

Add to your `docker-compose.yml`:

```yaml
version: '3.9'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  # Topics auto-create on producer/consumer access
```

---

## Quick Reference: Before vs After

| Aspect | Before | After |
|--------|--------|-------|
| Event Storage | Memory (lost on crash) | Outbox table (ACID) |
| Event Publishing | ApplicationEventPublisher (local) | Kafka (distributed) |
| Reliability | Best-effort (can lose data) | At-least-once (Kafka guarantee) |
| Audit Transaction | Separate (REQUIRES_NEW) | Same (same @Transactional) |
| Saga Pattern | None | OrderConfirmedSaga |
| Compensation | None | rollback stock, cancel order |
| Kafka Integration | None | Full event-driven |
| Idempotency | None | Event ID based |

