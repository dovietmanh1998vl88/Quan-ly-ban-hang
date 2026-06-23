package com.example.qlbh.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {

  @Query("SELECT o FROM OutboxEntity o WHERE o.processed = false ORDER BY o.createdAt ASC")
  List<OutboxEntity> findUnprocessed();

  @Query("SELECT o FROM OutboxEntity o WHERE o.processed = true AND o.processedAt < ?1")
  List<OutboxEntity> findProcessedBefore(Instant before);

  @Query("SELECT o FROM OutboxEntity o WHERE o.aggregateId = ?1 AND o.aggregateType = ?2")
  List<OutboxEntity> findByAggregate(String aggregateId, String aggregateType);
}
