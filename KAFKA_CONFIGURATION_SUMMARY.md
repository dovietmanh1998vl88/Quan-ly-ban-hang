# Kafka Configuration Summary

## Changes Made

### 1. ✅ docker-compose.yml

**Fixed Kafka broker listeners:**
```diff
- KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
+ KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093

- KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
+ KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092

- KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
+ KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
```

**Added Kafka bootstrap server to app environment:**
```yaml
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

**Fixed kafka condition from `service_started` to `service_healthy`:**
```yaml
depends_on:
  kafka:
    condition: service_healthy
```

**Added log retention settings:**
```yaml
KAFKA_LOG_RETENTION_HOURS: 168
KAFKA_LOG_SEGMENT_BYTES: 1073741824
```

### 2. ✅ application.yml

**Made bootstrap server configurable:**
```yaml
kafka:
  bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

**Fixed consumer deserializer trusted packages:**
```diff
- spring.json.trusted.packages: com.example.qlbh.domain.order.event
+ spring.json.trusted.packages: com.example.qlbh.domain.*,com.example.qlbh.application.*
```

**Added type mapping for events:**
```yaml
spring.json.type.mapping: "orderConfirmedEvent:com.example.qlbh.domain.order.event.OrderConfirmedEvent,auditLogCreatedEvent:com.example.qlbh.domain.audit.event.AuditLogCreatedEvent"
```

### 3. ✅ KafkaConfig.java

**Complete rewrite:**
- Removed hardcoded `localhost:9092`
- Removed duplicate producer factory configuration
- Now uses Spring Boot auto-config from `application.yml`
- Added `KafkaProperties` injection
- Customizes only listener container factory
- Properly configures JsonDeserializer with trusted packages
- Added error handling with DefaultErrorHandler

**Before:**
```java
// ❌ Hardcoded, wrong imports, duplicate config
private static final String BOOTSTRAP_SERVERS = "localhost:9092";
@Bean
public ProducerFactory<String, String> producerFactory() {
  // Hardcoded Kafka config...
}
```

**After:**
```java
// ✅ Uses Spring Boot auto-config, proper imports
@Bean
public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
  Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
  // Customize only what's needed
}
```

---

## Quick Start

### Start services with Docker Compose

```bash
# Start all services (MySQL, Redis, Kafka, MinIO, Spring app)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Clean up volumes (WARNING: deletes data)
docker-compose down -v
```

### Verify Kafka is running

```bash
# Check Kafka container is healthy
docker-compose ps

# View Kafka logs
docker-compose logs kafka

# Connect to Kafka and list topics
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

### Monitor with Kafka UI

Open browser: http://localhost:8081

---

## Configuration Hierarchy

```
1. application.yml (defaults)
   ↓ overridden by ↓
2. application-{profile}.yml (dev, test, prod)
   ↓ overridden by ↓
3. Environment variables (SPRING_KAFKA_*)
   ↓ used by ↓
4. KafkaConfig.java (listener customization)
```

**Example:**
```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    #                   ↑ environment variable
    #                                            ↑ default if not set
```

When running in Docker:
```yaml
# docker-compose.yml sets:
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

---

## Port Mapping

| Service | Internal Port | External Port | Purpose |
|---------|---------------|---------------|---------|
| Kafka (clients) | 9092 | 9092 | Producer/Consumer connection |
| Kafka (controller) | 9093 | - | Internal cluster communication |
| Kafka UI | 8080 | 8081 | Web browser monitoring |
| MySQL | 3306 | 3306 | Database |
| Redis | 6379 | 6379 | Cache |
| MinIO | 9000 | 9000 | Object storage |
| Spring App | 8080 | 8080 | API server |

---

## Environment-Specific Configuration

### Local Development (localhost)

```bash
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
# OR just run docker-compose locally, it sets it automatically
```

### Docker Compose (container-to-container)

```yaml
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Production (multiple brokers)

```bash
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092
```

---

## Verify Configuration

### Check if Kafka is accessible from app

```bash
docker-compose exec app ping kafka -c 3
# Should get pong responses

# Or check Java connectivity
docker-compose exec app bash
java -cp /app/lib/*:/app/lib:/app/config \
  org.apache.kafka.tools.DescribeClient \
  --bootstrap-server kafka:9092 \
  --type brokers
```

### View application.yml as loaded by Spring Boot

```bash
# Enable debug logging
docker-compose exec app curl http://localhost:8080/actuator/env/spring.kafka.bootstrap-servers

# In logs, Spring Boot prints active properties
docker-compose logs app | grep -i "kafka\|bootstrap"
```

---

## Common Commands

### Create topic manually

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic qlbh.order.confirmed \
  --partitions 3 \
  --replication-factor 1
```

### Delete topic

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic qlbh.order.confirmed
```

### Send test message

```bash
docker exec -it shop_kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic qlbh.order.confirmed

# Type message and press Enter:
{"orderId":"order-123","customerId":"cust-456","items":[],"totalAmount":"1000","occurredAt":"2024-01-01T00:00:00Z"}
```

### Consume messages

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic qlbh.order.confirmed \
  --from-beginning
```

### Check consumer group offset

```bash
docker exec shop_kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group order-saga-group \
  --describe
```

---

## Configuration Verification Checklist

- ✅ `docker-compose.yml` uses KRaft (no Zookeeper)
- ✅ Kafka advertised listeners point to `kafka:9092` (not localhost)
- ✅ App environment has `SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092`
- ✅ `application.yml` reads from environment variable
- ✅ `KafkaConfig.java` doesn't hardcode values
- ✅ Producer uses JsonSerializer
- ✅ Consumer uses JsonDeserializer with trusted packages
- ✅ Listener factory configured for manual commit
- ✅ All @KafkaListener methods are @Transactional
- ✅ Topics follow naming: `qlbh.<entity>.<action>`

---

## References

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Apache Kafka KRaft](https://kafka.apache.org/documentation/#raft)
- [Kafka-UI Documentation](https://docs.kafkaui.com/)

