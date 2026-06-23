package com.example.qlbh.infrastructure.config;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConfig
 *
 * Configures Kafka consumer containers for @KafkaListener.
 *
 * Producer config: configured by Spring Boot from application.yml
 * Consumer base config: configured by Spring Boot from application.yml
 *
 * This class customizes the listener container factory (concurrency, error handling, etc.)
 */
@Configuration
@EnableKafka
public class KafkaConfig {

  /**
   * Create consumer factory with JSON deserialization support.
   *
   * Spring Boot auto-configures from application.yml, but we need to
   * ensure trusted packages for JsonDeserializer.
   */
  @Bean
  public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

    // Ensure JsonDeserializer is configured with trusted packages
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, String.class.getName());
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.qlbh.domain.*,com.example.qlbh.application.*");

    return new DefaultKafkaConsumerFactory<>(props);
  }

  /**
   * Kafka listener container factory for @KafkaListener methods.
   *
   * Configuration:
   * - Concurrency: 3 threads per partition
   * - Ack mode: MANUAL (requires explicit acknowledgment)
   * - Batch listener: false (single message at a time)
   * - Error handling: log and retry
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(consumerFactory);
    factory.setConcurrency(3);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

    // Error handling
    factory.setCommonErrorHandler(
        new org.springframework.kafka.listener.DefaultErrorHandler()
    );

    return factory;
  }

  /**
   * Alternative batch listener factory (if needed in future).
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> batchListenerFactory(
      ConsumerFactory<String, String> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(consumerFactory);
    factory.setBatchListener(true);
    factory.setConcurrency(1);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

    return factory;
  }
}

