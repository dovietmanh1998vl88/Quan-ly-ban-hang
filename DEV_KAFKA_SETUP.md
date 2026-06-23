# Dev Environment Kafka Configuration

## ✅ Updated Files

### 1. docker-compose.dev.yml

**Added Kafka service:**
```yaml
kafka:
  image: apache/kafka:3.9.1
  container_name: shop_kafka_dev
  ports:
    - "9092:9092"
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_LOG_RETENTION_HOURS: 168
    KAFKA_LOG_SEGMENT_BYTES: 1073741824
  volumes:
    - kafka_dev_data:/var/lib/kafka/data
  healthcheck:
    test: ["CMD-SHELL", "/opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list || exit 1"]
```

**Added Kafka UI service:**
```yaml
kafka-ui:
  image: provectuslabs/kafka-ui:latest
  container_name: shop_kafka_ui_dev
  ports:
    - "8081:8080"
  environment:
    KAFKA_CLUSTERS_0_NAME: local
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
```

**Updated app service:**
```yaml
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092

depends_on:
  kafka:
    condition: service_healthy
```

**Added volume:**
```yaml
volumes:
  kafka_dev_data:
```

### 2. application-dev.yml

**Updated Kafka configuration:**
```yaml
kafka:
  bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
  
  # Updated trusted packages
  consumer:
    properties:
      spring.json.trusted.packages: com.example.qlbh.domain.*,com.example.qlbh.application.*
      spring.json.type.mapping: "orderConfirmedEvent:com.example.qlbh.domain.order.event.OrderConfirmedEvent"
```

---

## 🚀 Dev Environment Quick Start

### Start Dev Services

```bash
# Start all dev services (MySQL, Keycloak, Redis, Kafka, MinIO, Spring app)
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f app

# Stop all services
docker-compose -f docker-compose.dev.yml down
```

### Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Spring API | http://localhost:8080 | - |
| Keycloak | http://localhost:8180 | admin/admin |
| Kafka UI | http://localhost:8081 | - |
| MySQL | localhost:3306 | root/123456 |
| Redis | localhost:6379 | - |
| MinIO | http://localhost:9000 | admin/admin123456 |

### Verify Kafka is Running

```bash
# List containers
docker-compose -f docker-compose.dev.yml ps

# Check Kafka logs
docker-compose -f docker-compose.dev.yml logs kafka

# List Kafka topics
docker exec shop_kafka_dev /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:9092 --list
```

---

## Configuration Comparison

### Production (docker-compose.yml)
```yaml
- Container names: shop_kafka, shop_kafka_ui
- Kafka bootstrap: kafka:9092 (Docker network)
- App environment: SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Development (docker-compose.dev.yml)
```yaml
- Container names: shop_kafka_dev, shop_kafka_ui_dev
- Kafka bootstrap: kafka:9092 (Docker network - same)
- App environment: SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092 (same)
```

**Key:** Both environments use the same Kafka bootstrap server because both run in Docker with service name resolution.

---

## Spring Boot Configuration Hierarchy

### Development Profile

```
1. application.yml (base config)
   - bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

2. application-dev.yml (dev overrides)
   - bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
   - Expanded logging levels
   - Detailed SQL logging

3. docker-compose.dev.yml (environment variables)
   - SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Environment Variable Resolution

When running with `docker-compose -f docker-compose.dev.yml up`:

```
1. docker-compose sets: SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
2. Spring Boot loads application.yml (base)
3. Spring Boot loads application-dev.yml (dev overrides)
4. Environment variable ${SPRING_KAFKA_BOOTSTRAP_SERVERS} = kafka:9092
5. Final Kafka bootstrap: kafka:9092
```

---

## Local Development (Without Docker)

If running Spring Boot locally on your machine:

```bash
# Set environment variable
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# OR start Kafka locally
# Still need: MySQL, Redis, Keycloak, MinIO running

# Start only Kafka from docker-compose
docker-compose -f docker-compose.dev.yml up -d kafka kafka-ui
```

---

## File Structure

```
project/
├── docker-compose.yml          # Production config
├── docker-compose.dev.yml      # Dev config (WITH Kafka)
├── src/main/resources/
│   ├── application.yml         # Base config (uses env vars)
│   └── application-dev.yml     # Dev overrides
├── KAFKA_SETUP.md              # Kafka setup guide
├── KAFKA_CONFIGURATION_SUMMARY.md
└── IMPLEMENTATION_CHECKLIST.md
```

---

## Troubleshooting

### Issue: "Connection refused: kafka:9092"

**Cause:** App trying to connect to Kafka before it's ready

**Fix:**
```yaml
# Ensure app waits for Kafka
depends_on:
  kafka:
    condition: service_healthy  # ← not just service_started
```

### Issue: Kafka container exiting immediately

**Cause:** KRaft cluster not initialized

**Fix:** Check logs and wait 10-20 seconds for initialization
```bash
docker-compose -f docker-compose.dev.yml logs kafka
```

### Issue: "topic not found"

**Cause:** Topics auto-created on first message, or you're using wrong topic name

**Fix:**
```bash
# Verify topic exists
docker exec shop_kafka_dev /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:9092 --list

# Create topic if missing
docker exec shop_kafka_dev /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:9092 \
  --create \
  --topic qlbh.order.confirmed \
  --partitions 3 \
  --replication-factor 1
```

---

## Monitoring & Debugging

### View Application Logs with Kafka output

```bash
docker-compose -f docker-compose.dev.yml logs -f app | grep -i kafka
```

### Monitor Kafka Broker

```bash
# Watch Kafka logs
docker-compose -f docker-compose.dev.yml logs -f kafka

# Describe Kafka broker
docker exec shop_kafka_dev /opt/kafka/bin/kafka-broker-api-versions.sh \
  --bootstrap-server kafka:9092
```

### Test Message Publishing

```bash
# Send test message
echo '{"orderId":"test-order","customerId":"test-cust"}' | \
docker exec -i shop_kafka_dev /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server kafka:9092 \
  --topic qlbh.order.confirmed

# Consume messages
docker exec shop_kafka_dev /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic qlbh.order.confirmed \
  --from-beginning
```

### Check Consumer Group Status

```bash
docker exec shop_kafka_dev /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --group order-saga-group \
  --describe
```

---

## Performance Notes for Development

### Recommended Settings for Dev

```yaml
# application-dev.yml (optional overrides)
kafka:
  producer:
    batch-size: 16384          # Smaller for faster delivery
    linger-ms: 10              # Shorter wait
    buffer-memory: 33554432    # 32MB
    
  consumer:
    max-poll-records: 10       # Smaller batches for easier debugging
    session-timeout-ms: 30000  # Standard
```

### Volume Persistence

```yaml
# kafka_dev_data volume persists across restarts
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d
# Data is preserved!

# To clear data:
docker-compose -f docker-compose.dev.yml down -v
```

---

## Checklist: Dev Kafka Setup

- ✅ docker-compose.dev.yml has Kafka service
- ✅ docker-compose.dev.yml has Kafka UI service
- ✅ App environment has SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
- ✅ App depends_on kafka with service_healthy condition
- ✅ application-dev.yml has Kafka config with env var
- ✅ Kafka data persisted in kafka_dev_data volume
- ✅ Kafka UI accessible at http://localhost:8081
- ✅ Trusted packages expanded for JSON deserialization
- ✅ Type mapping configured for events
- ✅ All services have healthchecks

