package com.example.qlbh.infrastructure.event;

import com.example.qlbh.application.EventPublisher;
import com.example.qlbh.domain.DomainEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher
    implements EventPublisher {

  private final ApplicationEventPublisher publisher;

  @Override
  public void publish(
      List<DomainEvent> events) {

    events.forEach(
        publisher::publishEvent
    );
  }
}
