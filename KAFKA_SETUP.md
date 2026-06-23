# Kafka Configuration Setup Guide

## Overview

Your Kafka configuration is now standardized across:
1. **docker-compose.yml** - Kafka broker configuration
2. **application.yml** - Spring Boot Kafka properties
3. **KafkaConfig.java** - Listener container customization

---

## 1. Docker Compose Setup

### Kafka Broker Configuration

```yaml
kafka:
  image: apache/kafka:3.9.1
  container_name: shop_kafka
  ports:
    - "9092:9092"
  environment:
    # KRaft (no Zookeeper needed)
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    
    # Listeners for broker-to-broker and client access
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    
    # Controller configuration
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
    
    # Replication & retention
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_LOG_RETENTION_HOURS: 168
    KAFKA_LOG_SEGMENT_BYTES: 1073741824
```

### Key Settings Explained

| Setting | Value | Reason |
|---------|-------|--------|
| `KAFKA_LISTENERS` | `0.0.0.0:9092` | Accept from any host (Docker internal) |
| `KAFKA_ADVERTISED_LISTENERS` | `kafka:9092` | Tell clients to connect to `kafka:9092` (Docker DNS) |
| `KAFKA_CONTROLLER_QUORUM_VOTERS` | `1@kafka:9093` | Broker identity in KRaft cluster |
| `KAFKA_LOG_RETENTION_HOURS` | `168` | Keep messages 7 days (adjust as needed) |

### Spring Boot App Configuration

```yaml
environment:
  # Other configs...
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092  # ← Connect to Kafka in Docker
```

**Important:** Use `kafka:9092` (Docker service name) NOT `localhost:9092` when running in containers.

---

## 2. Spring Boot Configuration (application.yml)

### Producer Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
    producer:
      acks: all                              # Wait for all replicas
      retries: 2147483647                    # Retry forever (with backoff)
      enable-idempotence: true               # Exactly-once semantics
      max-in-flight-requests-per-connection: 5
      compression-type: snappy               # Reduce message size
      batch-size: 32768                      # 32KB batches
      linger-ms: 20                          # Wait 20ms for batching
      buffer-memory: 67108864                # 64MB send buffer
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Why these values?**
- `acks: all` - ensures all replicas acknowledge (durability)
- `enable-idempotence: true` - prevents duplicate messages
- `snappy` - good compression ratio without high CPU
- `batch-size: 32KB` - balance between latency and throughput
- `linger-ms: 20` - batch messages over 20ms window

### Consumer Configuration

```yaml
    consumer:
      group-id: order-group                  # Consumer group ID
      enable-auto-commit: false               # Manual ack
      auto-offset-reset: earliest             # Start from beginning if no offset
      max-poll-records: 100                   # Poll max 100 records at once
      session-timeout-ms: 30000               # 30s consumer timeout
      heartbeat-interval-ms: 10000            # Send heartbeat every 10s
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.example.qlbh.domain.*,com.example.qlbh.application.*
        spring.json.type.mapping: "orderConfirmedEvent:com.example.qlbh.domain.order.event.OrderConfirmedEvent"
```

**Important:**
- `enable-auto-commit: false` - we handle commits manually in listeners
- `auto-offset-reset: earliest` - start from oldest message if no offset exists
- `trusted.packages` - security: only deserialize from these packages
- `type.mapping` - map event names to classes (optional, for generic types)

---

## 3. Java Configuration (KafkaConfig.java)

### Listener Container Factory

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
    ConsumerFactory<String, String> consumerFactory) {
  ConcurrentKafkaListenerContainerFactory<String, String> factory =
      new ConcurrentKafkaListenerContainerFactory<>();

  factory.setConsumerFactory(consumerFactory);
  factory.setConcurrency(3);  // 3 concurrent threads per partition
  factory.getContainerProperties()
      .setAckMode(ContainerProperties.AckMode.MANUAL);  // Manual acknowledgment

  factory.setCommonErrorHandler(
      new org.springframework.kafka.listener.DefaultErrorHandler()
  );

  return factory;
}
```

**Settings:**
- `setConcurrency(3)` - process 3 messages in parallel per partition
- `AckMode.MANUAL` - explicitly acknowledge after successful processing
- `DefaultErrorHandler` - retry failed messages

---

## 4. Using in @KafkaListener

### Example: OrderConfirmedSaga

```java
@Component
public class OrderConfirmedSaga {

  @KafkaListener(
      topics = "qlbh.order.confirmed",
      groupId = "order-saga-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  @Transactional
  public void handleOrderConfirmed(String payload) {
    log.info("Processing: {}", payload);

    try {
      OrderConfirmedEvent event = objectMapper.readValue(payload, OrderConfirmedEvent.class);
      // Process event...
      // Acknowledgment happens automatically on successful completion
    } catch (Exception e) {
      log.error("Failed", e);
      throw e;  // Trigger retry
    }
  }
}
```

**Flow:**
1. Kafka polls messages
2. Method invoked with message
3. If `@Transactional` succeeds → message acknowledged
4. If exception thrown → retry (configurable)

### Example: AuditLogKafkaConsumer

```java
@Component
public class AuditLogKafkaConsumer {

  @KafkaListener(
      topics = "qlbh.audit.log.created",
      groupId = "audit-processor-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  @Transactional
  public void consumeAuditLog(String payload) {
    AuditLogCreatedEvent event = objectMapper.readValue(payload, AuditLogCreatedEvent.class);
    auditRepository.save(event.getAuditLog());
  }
}
```

---

## 5. Topic Creation

Kafka auto-creates topics on first message, but you can pre-create with desired settings:

```bash
# Connect to Kafka container
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic qlbh.order.confirmed \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000  # 7 days

docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic qlbh.audit.log.created \
  --partitions 1 \
  --replication-factor 1
```

### Topic Naming Convention

```
qlbh.<entity>.<action>

Examples:
├── qlbh.order.confirmed        (OrderConfirmedEvent)
├── qlbh.order.cancelled        (OrderCancelledEvent)
├── qlbh.product.stock.updated  (ProductStockUpdatedEvent)
├── qlbh.product.stock.released (ProductStockReleasedEvent)
└── qlbh.audit.log.created      (AuditLogCreatedEvent)
```

---

## 6. Monitoring & Debugging

### View Topics

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

### Describe Topic

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic qlbh.order.confirmed
```

### View Messages (Consumer)

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic qlbh.order.confirmed \
  --from-beginning
```

### Monitor via Kafka UI

Access http://localhost:8081 (already configured in docker-compose.yml)

---

## 7. Environment Variables

### Local Development

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Docker Environment (docker-compose)

```yaml
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Production

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-broker-1:9092,kafka-broker-2:9092,kafka-broker-3:9092
```

---

## 8. Troubleshooting

### Issue: "Failed to connect to Kafka"

**Cause:** Wrong bootstrap server
```
ERROR Failed to connect to cluster:
  localhost:9092 (connection refused)
```

**Fix:** Ensure `SPRING_KAFKA_BOOTSTRAP_SERVERS` matches docker-compose service name:
```yaml
# ❌ WRONG (in Docker containers)
SPRING_KAFKA_BOOTSTRAP_SERVERS: localhost:9092

# ✅ CORRECT (in Docker containers)
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Issue: "No serializer for object type"

**Cause:** Serializer mismatch
```
ERROR Cannot serialize to JSON: com.example.qlbh.domain.order.event.OrderConfirmedEvent
```

**Fix:** Ensure event class is serializable and implements `Serializable`:
```java
public class OrderConfirmedEvent implements DomainEvent, Serializable {
  private static final long serialVersionUID = 1L;
  // ...
}
```

### Issue: "Cannot deserialize to class"

**Cause:** Untrusted packages
```
ERROR java.lang.ClassNotFoundException: 
  com.example.qlbh.domain.order.event.OrderConfirmedEvent
```

**Fix:** Add package to `trusted.packages`:
```yaml
properties:
  spring.json.trusted.packages: com.example.qlbh.domain.*
```

### Issue: Consumer not receiving messages

**Cause:** Wrong consumer group ID
```yaml
consumer:
  group-id: wrong-group-id  # Different from where messages were sent
```

**Fix:** Ensure all consumers that should receive the same messages use same `group-id`.

---

## 9. Performance Tuning

### For High Throughput

```yaml
producer:
  batch-size: 65536              # 64KB (larger batches)
  linger-ms: 100                 # Wait longer for batching
  buffer-memory: 134217728       # 128MB
  compression-type: snappy
```

### For Low Latency

```yaml
producer:
  batch-size: 1                  # Send immediately
  linger-ms: 0                   # Don't wait
  buffer-memory: 33554432        # 32MB
  compression-type: none
```

### For Reliability

```yaml
producer:
  acks: all
  enable-idempotence: true
  max-in-flight-requests-per-connection: 5
  retries: 2147483647

consumer:
  enable-auto-commit: false      # Manual ack only
  session-timeout-ms: 30000      # Longer timeout
  max-poll-records: 100
```

---

## 10. Complete Checklist

- ✅ docker-compose.yml uses `kafka:9092` for advertised listeners
- ✅ app service sets `SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092`
- ✅ application.yml uses environment variable: `${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`
- ✅ KafkaConfig.java uses Spring Boot auto-config (not hardcoded)
- ✅ Producer: JsonSerializer, `acks: all`, `idempotence: true`
- ✅ Consumer: JsonDeserializer, manual commit, trusted packages
- ✅ Listener factory: concurrency 3, manual ack mode
- ✅ @KafkaListener methods are @Transactional
- ✅ Outbox events serialized as JSON strings
- ✅ Topics follow naming convention: `qlbh.<entity>.<action>`

