package com.example.qlbh.presentation.audit;

import com.example.qlbh.application.audit.dto.AuditLogDto;
import com.example.qlbh.application.audit.usecase.SearchAuditLogUseCase;
import com.example.qlbh.common.response.BaseResponse;
import com.example.qlbh.common.response.PageResponse;
import com.example.qlbh.domain.audit.model.AuditAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Xem lịch sử hoạt động hệ thống — chỉ ADMIN")
public class AuditController {

  private final SearchAuditLogUseCase searchAuditLogUseCase;

  /**
   * GET /audit?actorId=&entityType=Product&action=PRODUCT_DELETE&from=&to=&page=1&size=20
   * <p>
   * Tất cả params đều optional → flexible query cho admin.
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Tìm kiếm audit log — ADMIN only")
  public BaseResponse<PageResponse<AuditLogDto>> search(
      @RequestParam(required = false) String actorId,
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) AuditAction action,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    PageResponse<AuditLogDto> result = searchAuditLogUseCase.execute(
        actorId, entityType, action, from, to, page, size
    );
    return BaseResponse.success(result);
  }
}