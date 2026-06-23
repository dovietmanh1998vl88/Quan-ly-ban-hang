# Event-Driven Architecture: Aggregate → Outbox → Kafka → Saga

## 1. AGGREGATE REGISTERS BUSINESS EVENT

```
Order.confirm()
  ↓
registerEvent(OrderConfirmedEvent)
  ↓
Event CHỈ lưu trong memory của Aggregate
```

**Tại sao?** Aggregate là nơi duy nhất biết business logic và side-effects cần trigger.

---

## 2. APPLICATION LAYER: TRANSACTIONAL SAVE

```
@Transactional
public void confirmOrder(String orderId):
  1. order = repository.findById(orderId)
  2. order.confirm()
  3. repository.save(order)  // Persist aggregate
  4. Publish events to Outbox  // CÙNG TRANSACTION
  5. Publish audit log to Outbox  // CÙNG TRANSACTION
```

**Tại sao Transaction?** Nếu save aggregate succeed nhưng outbox fail → event mất
Nếu save aggregate fail → event không được ghi

---

## 3. OUTBOX PATTERN: RELIABLE EVENT STORAGE

```
Table: outbox_events
┌─────────┬──────────────┬─────────┬──────────┐
│ id      │ eventType    │ payload │ processed│
├─────────┼──────────────┼─────────┼──────────┤
│ uuid-1  │ OrderConfirmed│ {...}  │ false    │ ← OutboxPoller polls here
│ uuid-2  │ AuditLogCreated│{...}  │ false    │
└─────────┴──────────────┴─────────┴──────────┘
```

**Tại sao?** Ensures At-Least-Once delivery → Kafka receives event

---

## 4. OUTBOX POLLER: SCHEDULED TASK

```
@Scheduled(fixedDelay = 1000)
processOutbox():
  1. Get unprocessed events
  2. Publish to Kafka (idempotent)
  3. Mark as processed
```

**Tại sao?** Decouples DB transaction từ Kafka publish

---

## 5. SAGA PATTERN: ORCHESTRATION

```
OrderConfirmedEvent → OrderConfirmedSaga
  1. Reserve stock (ProductStockReservationService)
  2. Update inventory → ProductStockUpdatedEvent
  3. Publish to Outbox
  
If fail → Compensate (undo stock reservation)
```

**Tại sao?** Handles long-running business processes & rollback

---

## 6. AUDIT EVENTS: WHEN TO PUBLISH?

**Option A: Publish with Business Event (RECOMMENDED)**
```
confirmOrder():
  order.confirm()
  registerEvent(OrderConfirmedEvent)
  registerEvent(AuditLogCreatedEvent)  // cùng lúc
  repository.save(order)
  outboxEventService.publishEventsToOutbox(order.getEvents())
```

**Option B: Listen to Business Event (DECOUPLED)**
```
OrderConfirmedSaga:
  onOrderConfirmed():
    auditService.logAction(AUDIT_ORDER_CONFIRMED, ...)
```

**Khác biệt:**
- Option A: Audit log cùng transaction với order (consistent)
- Option B: Audit log là side-effect (eventual consistency)

Dùng **Option A** vì audit phải track đúng những thay đổi.

---

## 7. KAFKA TOPICS STRUCTURE

```
Kafka Topics:
├── order.confirmed (OrderConfirmedEvent)
├── order.cancelled (OrderCancelledEvent)
├── stock.reserved (ProductStockUpdatedEvent)
├── stock.released (ProductStockReleaseEvent)
└── audit.log (AuditLogCreatedEvent)

Consumers:
├── OrderConfirmedSaga → order.confirmed
├── StockReservationService → order.confirmed
└── AuditLogProcessor → audit.log
```

---

## 8. IDEMPOTENCY & DEDUPLICATION

```
Kafka message:
{
  "eventId": "uuid-xxxx",  // ← unique
  "eventType": "OrderConfirmed",
  "timestamp": 1234567890,
  "payload": {...}
}

Consumer:
if (idempotencyStore.exists(eventId)):
  skip()
else:
  process()
  idempotencyStore.save(eventId)
```

**Tại sao?** Kafka có thể deliver duplicate messages → Must handle

---

## FLOW DIAGRAM

```
┌──────────────────────────────────────────────────────────────────┐
│ REQUEST: POST /api/orders/{id}/confirm                           │
└──────────────────────────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER: ConfirmOrderUseCase                            │
│  @Transactional                                                  │
│  ├─ order = repository.find(id)                                  │
│  ├─ order.confirm()  // register OrderConfirmedEvent            │
│  ├─ repository.save(order)                                       │
│  ├─ auditService.log(...)  // register AuditLogCreatedEvent     │
│  └─ outboxService.publish(order.getEvents())  // same TX         │
└──────────────────────────────────────────────────────────────────┘
         ↓ (Outbox table)
┌──────────────────────────────────────────────────────────────────┐
│ OUTBOX_EVENTS TABLE (Persistent)                                 │
│  ├─ OrderConfirmedEvent → processed=false                        │
│  └─ AuditLogCreatedEvent → processed=false                       │
└──────────────────────────────────────────────────────────────────┘
         ↓ (Polling)
┌──────────────────────────────────────────────────────────────────┐
│ OutboxPoller @Scheduled(fixedDelay=1000)                          │
│  → Publish to Kafka                                               │
│  → Mark processed=true                                            │
└──────────────────────────────────────────────────────────────────┘
         ↓
┌──────────────────────────────────────────────────────────────────┐
│ KAFKA (Reliable Message Bus)                                      │
│  topic: order.confirmed → OrderConfirmedSaga                     │
│  topic: audit.log → AuditLogProcessor                            │
└──────────────────────────────────────────────────────────────────┘
         ↓
┌──────────────────────────────────────────────────────────────────┐
│ SAGA ORCHESTRATION (Long-running transactions)                    │
│ OrderConfirmedSaga:                                               │
│  ├─ order.confirmed → Stock Reservation Service                 │
│  ├─ Register ProductStockUpdatedEvent                            │
│  ├─ Publish to Outbox                                            │
│  └─ If fail: Compensate (rollback)                               │
└──────────────────────────────────────────────────────────────────┘
         ↓
┌──────────────────────────────────────────────────────────────────┐
│ AUDIT PROCESSING (Async)                                          │
│ AuditLogProcessor:                                                │
│  ├─ Consume audit.log                                             │
│  ├─ Store in AuditLog table                                      │
│  ├─ Send alerts if security issue                                │
│  └─ Forward to ELK/Datadog                                        │
└──────────────────────────────────────────────────────────────────┘
```

---

## KEY RULES

1. **Only Aggregate registers business events**
2. **Audit event published SAME transaction as business event**
3. **Events saved to Outbox (same DB transaction)**
4. **Outbox Poller publishes to Kafka**
5. **Saga consumes business events, NOT audit events**
6. **Saga can publish new business events (compensating transactions)**
7. **Audit is FIRE-AND-FORGET, business events are CRITICAL**
