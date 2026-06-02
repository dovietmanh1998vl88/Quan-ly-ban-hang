package com.example.qlbh.presentation.oder;

import com.example.qlbh.application.order.command.AddItemCommand;
import com.example.qlbh.application.order.command.CancelOrderCommand;
import com.example.qlbh.application.order.command.ConfirmOrderCommand;
import com.example.qlbh.application.order.command.CreateOrderCommand;
import com.example.qlbh.application.order.dto.OrderDto;
import com.example.qlbh.application.order.usecase.AddItemToOrderUseCase;
import com.example.qlbh.application.order.usecase.CancelOrderUseCase;
import com.example.qlbh.application.order.usecase.ConfirmOrderUseCase;
import com.example.qlbh.application.order.usecase.CreateOrderUseCase;
import com.example.qlbh.application.order.usecase.GetOrderUseCase;
import com.example.qlbh.common.response.BaseResponse;
import com.example.qlbh.common.util.SecurityUtils;
import com.example.qlbh.presentation.oder.request.AddItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// presentation/order/OrderController.java
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Quản lý đơn hàng")
public class OrderController {

  private final CreateOrderUseCase createOrderUseCase;
  private final AddItemToOrderUseCase addItemUseCase;
  private final ConfirmOrderUseCase confirmOrderUseCase;
  private final CancelOrderUseCase cancelOrderUseCase;
  private final GetOrderUseCase getOrderUseCase;

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
  @Operation(summary = "Tạo đơn hàng mới")
  public BaseResponse<OrderDto> createOrder() {
    // customerId lấy từ token — không tin client
    String customerId = SecurityUtils.getCurrentUserId();
    OrderDto dto = createOrderUseCase.execute(
        new CreateOrderCommand(customerId)
    );
    return BaseResponse.success("Tạo đơn hàng thành công", dto);
  }

  @PostMapping("/{orderId}/items")
  @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
  @Operation(summary = "Thêm sản phẩm vào đơn hàng")
  public BaseResponse<OrderDto> addItem(
      @PathVariable String orderId,
      @Valid @RequestBody AddItemRequest request
  ) {
    AddItemCommand command = new AddItemCommand();
    command.setOrderId(orderId);
    command.setProductId(request.productId());
    command.setQuantity(request.quantity());

    return BaseResponse.success(addItemUseCase.execute(command));
  }

  @PutMapping("/{orderId}/confirm")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  @Operation(summary = "Xác nhận đơn hàng — trừ stock")
  public BaseResponse<OrderDto> confirm(@PathVariable String orderId) {
    return BaseResponse.success(
        confirmOrderUseCase.execute(new ConfirmOrderCommand(orderId))
    );
  }

  @PutMapping("/{orderId}/cancel")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
  @Operation(summary = "Hủy đơn hàng")
  public BaseResponse<OrderDto> cancel(@PathVariable String orderId) {
    // CUSTOMER chỉ cancel được order của mình
    // ADMIN/STAFF cancel được tất cả
    String customerId = null;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isCustomer = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

    if (isCustomer) {
      customerId = SecurityUtils.getCurrentUserId();
    }

    return BaseResponse.success(
        cancelOrderUseCase.execute(
            new CancelOrderCommand(orderId, customerId)
        )
    );
  }

  @GetMapping("/{orderId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
  @Operation(summary = "Lấy thông tin đơn hàng")
  public BaseResponse<OrderDto> getOrder(@PathVariable String orderId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isCustomer = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

    String customerId = isCustomer
        ? SecurityUtils.getCurrentUserId()
        : null;
    return BaseResponse.success(
        getOrderUseCase.execute(orderId, customerId)
    );
  }
}