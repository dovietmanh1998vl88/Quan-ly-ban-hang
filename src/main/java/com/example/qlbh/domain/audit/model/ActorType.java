package com.example.qlbh.domain.audit.model;

public enum ActorType {
  USER,       // người dùng thật (từ Keycloak JWT)
  SYSTEM,     // scheduled job, startup, automated task
  SERVICE     // service-to-service call
}