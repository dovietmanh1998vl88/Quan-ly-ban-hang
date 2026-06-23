package com.example.qlbh.common.annotation;

import com.example.qlbh.domain.audit.model.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @AuditLog — annotation đánh dấu method cần được audit.
 * <p>
 * Dùng trên Controller method (hoặc UseCase method). AuditAspect (AOP) sẽ intercept và tự động tạo audit log.
 * <p>
 * Ví dụ:
 * <pre>
 *   @PostMapping
 *   @AuditLog(action = AuditAction.PRODUCT_CREATE, entityType = "Product")
 *   public BaseResponse<ProductResponse> createProduct(...) { ... }
 * </pre>
 * <p>
 * Tại sao không hardcode trong service? → Cross-cutting concern nên tách ra khỏi business logic (SRP + DIP) → Thêm/bỏ
 * audit không cần sửa service → Dễ test service mà không cần mock audit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

  /**
   * Action được thực hiện
   */
  AuditAction action();

  /**
   * Tên entity bị tác động
   */
  String entityType() default "";

  /**
   * SpEL expression để lấy entityId từ method arguments. Ví dụ: "#id" để lấy param tên "id", "#command.productId" để
   * lấy field
   */
  String entityIdExpression() default "";

  /**
   * Mô tả tĩnh — nếu không set, AuditAspect sẽ tự sinh description từ action name.
   */
  String description() default "";

  /**
   * Có log request payload không. Tắt mặc định vì payload có thể chứa sensitive data.
   */
  boolean logPayload() default false;
}