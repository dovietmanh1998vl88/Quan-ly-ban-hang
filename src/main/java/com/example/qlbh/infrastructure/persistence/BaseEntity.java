package com.example.qlbh.infrastructure.persistence;

import com.example.qlbh.common.util.UuidGenerator;
import jakarta.persistence.*;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  @Id
  @Column(
      name = "id",
      columnDefinition = "VARCHAR(36)",  // UUID lưu dạng String trong MySQL
      updatable = false,
      nullable = false
  )
  protected String id;

  @CreatedDate
  @Column(updatable = false)
  protected Instant createdAt;

  @LastModifiedDate
  protected Instant updatedAt;

  @CreatedBy
  protected String createdBy;

  @LastModifiedBy
  protected String updatedBy;

  /**
   * @PrePersist — tự động generate UUID trước khi insert. Không dùng @GeneratedValue vì UUID do application tạo, không
   * phải DB tạo — đây là điểm khác biệt quan trọng với AUTO_INCREMENT.
   * <p>
   * Lợi ích: biết id TRƯỚC KHI save xuống DB → có thể dùng id trong business logic trước khi persist
   */
  @PrePersist
  protected void generateId() {
    if (this.id == null) {
      this.id = UuidGenerator.generateString();
    }
  }
}