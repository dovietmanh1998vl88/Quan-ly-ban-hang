package com.example.qlbh.infrastructure.persistence.oder.repository;

import com.example.qlbh.infrastructure.persistence.oder.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository
    extends JpaRepository<OrderEntity, String> {

  List<OrderEntity> findByCustomerId(String customerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
  Optional<OrderEntity> findByIdForUpdate(@Param("id") String id);


  @Query("""
          SELECT o
          FROM OrderEntity o
          WHERE FUNCTION('DATE', o.createdAt)
                BETWEEN :fromDate AND :toDate
      """)
  List<OrderEntity> findByCreatedAtBetween(
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);
}