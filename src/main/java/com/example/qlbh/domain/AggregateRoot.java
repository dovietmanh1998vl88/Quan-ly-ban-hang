package com.example.qlbh.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {

  private final List<DomainEvent> events =
      new ArrayList<>();

  protected void registerEvent(
      DomainEvent event) {

    events.add(event);
  }

  public List<DomainEvent> getEvents() {
    return events;
  }

  public void clearEvents() {
    events.clear();
  }
}
