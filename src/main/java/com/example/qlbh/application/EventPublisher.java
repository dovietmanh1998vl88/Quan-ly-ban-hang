package com.example.qlbh.application;

import com.example.qlbh.domain.DomainEvent;
import java.util.List;

public interface EventPublisher {

  void publish(
      List<DomainEvent> events
  );
}
