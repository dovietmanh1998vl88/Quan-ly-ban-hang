package com.example.qlbh.common.util;

import com.fasterxml.uuid.Generators;
import java.util.UUID;

/**
 * UUID v7 — time-ordered UUID.
 * <p>
 * Tại sao v7 thay vì v4?
 * - v4 hoàn toàn random → insert vào B-tree index bị phân mảnh → MySQL phải rebalance index liên tục → chậm khi data
 * lớn
 * - v7 có timestamp prefix → insert tuần tự vào cuối index → performance gần bằng AUTO_INCREMENT nhưng không lộ thông
 * tin
 */
public final class UuidGenerator {

  private UuidGenerator() {
  }

  public static UUID generate() {
    return Generators.timeBasedEpochGenerator().generate();
  }

  public static String generateString() {
    return generate().toString();
  }
}