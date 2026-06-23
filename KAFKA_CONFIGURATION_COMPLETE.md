# Kafka Configuration Complete Setup

## 📦 All Environments Configured

✅ **Production** (docker-compose.yml)
✅ **Development** (docker-compose.dev.yml)
✅ **Local Java Config** (KafkaConfig.java)
✅ **Base Spring Config** (application.yml)
✅ **Dev Spring Config** (application-dev.yml)

---

## Quick Start Commands

### Production Environment

```bash
# Start
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop
docker-compose down
```

### Development Environment

```bash
# Start
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f app

# Stop
docker-compose -f docker-compose.dev.yml down
```

### Monitor Kafka UI

**Production:** http://localhost:8081
**Development:** http://localhost:8081 (same port)

---

## Configuration Files Summary

| File | Purpose | Kafka? |
|------|---------|--------|
| `docker-compose.yml` | Production services | ✅ Yes |
| `docker-compose.dev.yml` | Dev services | ✅ Yes |
| `KafkaConfig.java` | Spring listener config | ✅ Customizes |
| `application.yml` | Base Spring config | ✅ Base settings |
| `application-dev.yml` | Dev Spring config | ✅ Dev overrides |

---

## Configuration Hierarchy

```
Priority (highest to lowest):
1. Environment variables (docker-compose)
   ↓
2. application-{profile}.yml (application-dev.yml)
   ↓
3. application.yml (base config)
   ↓
4. KafkaConfig.java (listener factory only)
   ↓
5. Spring Boot defaults
```

### Example: Bootstrap Server Resolution

```
docker-compose.dev.yml sets:
  SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
         ↓
application-dev.yml reads:
  bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
         ↓
Final value: kafka:9092 (from Docker environment)
```

---

## Services & Ports

### Production (docker-compose.yml)

```yaml
Services:
├── kafka:9092 ← Kafka broker
├── kafka-ui:8081 ← Kafka UI
├── app:8080 ← Spring Boot API
├── mysql:3306 ← MySQL database
├── redis:6379 ← Redis cache
└── minio:9000 ← MinIO S3-like storage
```

### Development (docker-compose.dev.yml)

```yaml
Services:
├── kafka:9092 ← Kafka broker (same port)
├── kafka-ui:8081 ← Kafka UI (same port)
├── app:8080 ← Spring Boot API (with dev features)
├── mysql:3306 ← MySQL (shop_management)
├── mysql_keycloak:3307 ← MySQL (keycloak)
├── keycloak:8180 ← Keycloak IAM
├── redis:6379 ← Redis cache
└── minio:9000 ← MinIO storage
```

---

## Key Configuration Points

### 1. Docker Network (Service Names)

**In docker-compose:**
```yaml
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
```

**NOT `localhost:9092`** — because containers communicate via Docker DNS (service names)

### 2. Spring Boot Environment Variable

**In docker-compose:**
```yaml
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

**In application.yml:**
```yaml
kafka:
  bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

The `localhost:9092` is the fallback when running Spring Boot locally (not in Docker)

### 3. JSON Serialization

**application.yml:**
```yaml
producer:
  value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
consumer:
  value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
  properties:
    spring.json.trusted.packages: com.example.qlbh.domain.*,com.example.qlbh.application.*
```

**KafkaConfig.java:**
```java
// Ensures trusted packages are properly configured for JsonDeserializer
props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.qlbh.domain.*,...");
```

### 4. Listener Container Configuration

**KafkaConfig.java (only place that customizes listeners):**
```java
factory.setConcurrency(3);  // 3 threads per partition
factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
```

---

## Event Flow

### Complete Event Pipeline

```
1. OrderApplicationService.confirmOrder()
   ├─ order.confirm() → registers OrderConfirmedEvent (memory)
   ├─ repository.save(order) → persists aggregate
   ├─ auditService.createAuditLog() → registers AuditLogCreatedEvent
   └─ outboxService.publishEventsToOutbox(...) 
      ↓ (SAME @Transactional block)
      
2. Outbox Table (Database)
   ├─ OrderConfirmedEvent (processed=false)
   └─ AuditLogCreatedEvent (processed=false)
      ↓
      
3. OutboxPoller (runs every 1 second)
   ├─ Finds unprocessed events
   └─ Publishes to Kafka (using KafkaTemplate from KafkaConfig)
      ↓ (serialized as JSON)
      
4. Kafka Topics
   ├─ qlbh.order.confirmed (OrderConfirmedEvent as JSON)
   └─ qlbh.audit.log.created (AuditLogCreatedEvent as JSON)
      ↓
      
5. Consumers (via @KafkaListener with KafkaConfig factory)
   ├─ OrderConfirmedSaga (consumes qlbh.order.confirmed)
   │  ├─ Deserializes JSON to OrderConfirmedEvent
   │  ├─ Calls StockReservationService
   │  └─ Publishes ProductStockUpdatedEvent to Outbox
   │     ↓ (same @Transactional)
   │
   └─ AuditLogKafkaConsumer (consumes qlbh.audit.log.created)
      ├─ Deserializes JSON to AuditLogCreatedEvent
      └─ Persists to AuditLog table
```

---

## File Locations

```
project root/
├── docker-compose.yml                    (Production config)
├── docker-compose.dev.yml                (Dev config)
├── src/main/resources/
│   ├── application.yml                   (Base Spring config)
│   └── application-dev.yml               (Dev Spring config)
├── src/main/java/com/example/qlbh/
│   └── infrastructure/config/
│       └── KafkaConfig.java              (Listener customization)
└── docs/
    ├── ARCHITECTURE.md                   (High-level flow)
    ├── EVENT_DRIVEN_GUIDE.md             (Detailed guide)
    ├── KAFKA_SETUP.md                    (Kafka setup)
    ├── KAFKA_CONFIGURATION_SUMMARY.md    (Quick reference)
    ├── DEV_KAFKA_SETUP.md                (Dev-specific)
    └── KAFKA_CONFIGURATION_COMPLETE.md   (This file)
```

---

## Verification Steps

### 1. Verify Docker Compose Configuration

```bash
# Production
docker-compose config | grep -A 20 kafka:

# Development
docker-compose -f docker-compose.dev.yml config | grep -A 20 kafka:
```

### 2. Verify Spring Boot Configuration

```bash
# Check what Kafka properties Spring Boot sees
curl http://localhost:8080/actuator/env | jq '.propertySources[] | select(.name | contains("Kafka"))'
```

### 3. Verify Kafka is Running

```bash
# Check container health
docker-compose ps kafka

# Check Kafka logs for startup
docker-compose logs kafka | grep -i "started\|error"
```

### 4. Verify Topics Exist

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:9092 --list
```

---

## Troubleshooting Matrix

| Problem | Cause | Solution |
|---------|-------|----------|
| Connection refused | Wrong bootstrap server | Check `SPRING_KAFKA_BOOTSTRAP_SERVERS` env var |
| App won't start | Kafka not ready | Ensure `condition: service_healthy` |
| No topics | Topics not created | Auto-created on first message, or create manually |
| Deserialization error | Wrong trusted packages | Expand `spring.json.trusted.packages` |
| Messages stuck | Manual commit not configured | Ensure `enable-auto-commit: false` and @Transactional |
| Duplicate messages | No idempotency | Add event ID deduplication |

---

## Next Steps

1. **Start Dev Environment:**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. **Test Order Confirmation:**
   ```bash
   curl -X POST http://localhost:8080/api/orders/{id}/confirm \
     -H "Authorization: Bearer {token}"
   ```

3. **Monitor Kafka UI:**
   Open http://localhost:8081 and watch events flow

4. **Check Logs:**
   ```bash
   docker-compose -f docker-compose.dev.yml logs -f app
   ```

5. **Verify Event Processing:**
   - Check `outbox_events` table: should see events with `processed=true`
   - Check `audit_logs` table: should see audit entries
   - Check Kafka topics: should see event messages

---

## Documentation Reference

| Document | Content |
|----------|---------|
| `ARCHITECTURE.md` | Event flow diagrams and patterns |
| `EVENT_DRIVEN_GUIDE.md` | Complete implementation guide |
| `IMPLEMENTATION_CHECKLIST.md` | Step-by-step changes needed |
| `KAFKA_SETUP.md` | Kafka configuration details |
| `KAFKA_CONFIGURATION_SUMMARY.md` | Changes made and commands |
| `DEV_KAFKA_SETUP.md` | Dev environment specifics |
| `KAFKA_CONFIGURATION_COMPLETE.md` | This comprehensive reference |

---

## Summary

✅ **Kafka is now fully configured** across:
- Production environment (docker-compose.yml)
- Development environment (docker-compose.dev.yml)
- Spring Boot base config (application.yml)
- Spring Boot dev config (application-dev.yml)
- Java Kafka listener (KafkaConfig.java)

✅ **Key principles applied:**
- Configuration hierarchy: environment vars → dev config → base config → defaults
- Service discovery: using Docker service names (kafka:9092)
- Transactional consistency: Outbox pattern ensures delivery
- JSON serialization: with trusted packages security
- Manual commitment: explicit consumer acknowledgment
- Listener customization: only where needed

✅ **Ready to use:**
- Run `docker-compose up -d` or `docker-compose -f docker-compose.dev.yml up -d`
- Events flow: Aggregate → Outbox → Kafka → Saga
- Audit logging: integrated with business events

