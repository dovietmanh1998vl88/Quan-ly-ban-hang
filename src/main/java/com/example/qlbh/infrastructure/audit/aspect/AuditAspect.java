package com.example.qlbh.infrastructure.audit.aspect;

import com.example.qlbh.application.audit.command.AuditLogCommand;
import com.example.qlbh.application.audit.usecase.CreateAuditLogUseCase;
import com.example.qlbh.common.annotation.AuditLog;
import com.example.qlbh.domain.audit.model.ActorType;
import com.example.qlbh.domain.audit.model.AuditStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AuditAspect — AOP interceptor cho @AuditLog annotation.
 * <p>
 * Cách hoạt động:
 * 1. Intercept method có @AuditLog
 * 2. Lấy context (user, IP, user-agent) từ SecurityContext và HttpRequest
 * 3. Gọi method gốc → đo thời gian thực thi
 * 4. Tạo AuditLogCommand với kết quả (SUCCESS/FAILED)
 * 5. Gọi CreateAuditLogUseCase — non-blocking (REQUIRES_NEW transaction)
 * <p>
 * Business code KHÔNG biết gì về audit. Thêm/bỏ audit chỉ cần thêm/bỏ annotation.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

  private final CreateAuditLogUseCase createAuditLogUseCase;
  private final ObjectMapper objectMapper;

  private final ExpressionParser spelParser = new SpelExpressionParser();

  @Around("@annotation(auditLogAnnotation)")
  public Object audit(
      ProceedingJoinPoint joinPoint,
      AuditLog auditLogAnnotation
  ) throws Throwable {

    long startTime = System.currentTimeMillis();
    Object result = null;
    Throwable thrown = null;

    try {
      result = joinPoint.proceed();
      return result;

    } catch (Throwable ex) {
      thrown = ex;
      throw ex;

    } finally {
      long durationMs = System.currentTimeMillis() - startTime;

      // Tạo audit log trong finally — đảm bảo luôn ghi dù success hay fail
      try {
        saveAuditLog(joinPoint, auditLogAnnotation, result, thrown, durationMs);
      } catch (Exception e) {
        // Audit failure KHÔNG được làm hỏng business flow
        log.error("[AuditAspect] Failed to create audit log: {}", e.getMessage());
      }
    }
  }

  private void saveAuditLog(
      ProceedingJoinPoint joinPoint,
      AuditLog annotation,
      Object result,
      Throwable thrown,
      long durationMs
  ) {
    // ===== Extract actor từ SecurityContext =====
    String actorId = "system";
    String actorName = "system";
    ActorType actorType = ActorType.SYSTEM;

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()
        && !"anonymousUser".equals(auth.getPrincipal())) {

      if (auth instanceof JwtAuthenticationToken jwtAuth) {
        // Keycloak JWT
        Jwt jwt = jwtAuth.getToken();
        actorId = jwt.getSubject();
        actorName = jwt.getClaimAsString("preferred_username");
        actorType = ActorType.USER;
      } else {
        // Custom JWT filter (backward compat)
        actorName = auth.getName();
        actorId = actorName;
        actorType = ActorType.USER;
      }
    }

    // ===== Extract entityId từ SpEL expression =====
    String entityId = null;
    if (!annotation.entityIdExpression().isBlank()) {
      entityId = resolveSpel(annotation.entityIdExpression(), joinPoint);
    }

    // ===== Extract HTTP context =====
    String ipAddress = null;
    String userAgent = null;
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs != null) {
      HttpServletRequest req = attrs.getRequest();
      ipAddress = extractIp(req);
      userAgent = req.getHeader("User-Agent");
    }

    // ===== Extract request payload (opt-in) =====
    String requestPayload = null;
    if (annotation.logPayload()) {
      try {
        requestPayload = objectMapper.writeValueAsString(joinPoint.getArgs());
        // Giới hạn kích thước để tránh log payload quá lớn
        if (requestPayload != null && requestPayload.length() > 2000) {
          requestPayload = requestPayload.substring(0, 2000) + "...[truncated]";
        }
      } catch (Exception e) {
        requestPayload = "[serialize failed]";
      }
    }

    // ===== Build status và error =====
    AuditStatus status = thrown == null ? AuditStatus.SUCCESS : AuditStatus.FAILED;
    String errorMessage = thrown != null
        ? thrown.getClass().getSimpleName() + ": " + thrown.getMessage()
        : null;

    // ===== Build description =====
    String description = annotation.description().isBlank()
        ? buildAutoDescription(annotation, entityId, actorName)
        : annotation.description();

    // ===== Build và save command =====
    AuditLogCommand command = AuditLogCommand.builder()
        .actorId(actorId)
        .actorName(actorName)
        .actorType(actorType)
        .action(annotation.action())
        .entityType(annotation.entityType())
        .entityId(entityId)
        .description(description)
        .status(status)
        .errorMessage(errorMessage)
        .requestPayload(requestPayload)
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .durationMs(durationMs)
        .build();

    createAuditLogUseCase.execute(command);
  }

  /**
   * Resolve SpEL expression từ method arguments. Ví dụ: "#id" → lấy giá trị parameter tên "id" "#command.productId" →
   * lấy field productId của param tên "command"
   */
  private String resolveSpel(String expression, ProceedingJoinPoint joinPoint) {
    try {
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      String[] paramNames = signature.getParameterNames();
      Object[] args = joinPoint.getArgs();
      log.info("expression = {}", expression);
      log.info(
          "params = {}",
          Arrays.toString(paramNames)
      );
      StandardEvaluationContext context = new StandardEvaluationContext();
      for (int i = 0; i < paramNames.length; i++) {
        context.setVariable(paramNames[i], args[i]);
      }

      Object value = spelParser
          .parseExpression(expression)
          .getValue(context);

      return value != null ? value.toString() : null;

    } catch (Exception e) {
      log.warn("[AuditAspect] SpEL resolution failed for '{}': {}", expression, e.getMessage());
      return null;
    }
  }

  private String buildAutoDescription(AuditLog annotation, String entityId, String actorName) {
    StringBuilder sb = new StringBuilder();
    sb.append(actorName).append(" ");
    sb.append(annotation.action().name().replace("_", " ").toLowerCase());
    if (!annotation.entityType().isBlank()) {
      sb.append(" ").append(annotation.entityType());
    }
    if (entityId != null) {
      sb.append(" [").append(entityId).append("]");
    }
    return sb.toString();
  }

  /**
   * Lấy IP thật khi đằng sau reverse proxy
   */
  private String extractIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
      // X-Forwarded-For có thể chứa danh sách IP: "client, proxy1, proxy2"
      return ip.split(",")[0].trim();
    }
    ip = request.getHeader("X-Real-IP");
    if (ip != null && !ip.isBlank()) {
      return ip;
    }
    return request.getRemoteAddr();
  }
}