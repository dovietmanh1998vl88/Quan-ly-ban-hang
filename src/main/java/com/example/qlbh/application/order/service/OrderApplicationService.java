package com.example.qlbh.application.order.service;

import com.example.qlbh.application.order.command.AddItemCommand;
import com.example.qlbh.application.order.command.CancelOrderCommand;
import com.example.qlbh.application.order.command.ConfirmOrderCommand;
import com.example.qlbh.application.order.command.CreateOrderCommand;
import com.example.qlbh.application.order.dto.OrderDto;
import com.example.qlbh.application.order.dto.OrderItemPrintDto;
import com.example.qlbh.application.order.dto.OrderPrintDto;
import com.example.qlbh.application.order.mapper.OrderApplicationMapper;
import com.example.qlbh.application.order.usecase.AddItemToOrderUseCase;
import com.example.qlbh.application.order.usecase.CancelOrderUseCase;
import com.example.qlbh.application.order.usecase.ConfirmOrderUseCase;
import com.example.qlbh.application.order.usecase.CreateOrderUseCase;
import com.example.qlbh.application.order.usecase.GetOrderUseCase;
import com.example.qlbh.application.order.usecase.PrintOrderUseCase;
import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.common.exception.ForbiddenException;
import com.example.qlbh.common.exception.NotFoundException;
import com.example.qlbh.domain.order.model.Order;
import com.example.qlbh.domain.order.model.OrderItem;
import com.example.qlbh.domain.order.repository.OrderDomainRepository;
import com.example.qlbh.domain.order.valueobject.Money;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.repository.ProductDomainRepository;
import com.example.qlbh.domain.product.valueobject.Price;
import com.example.qlbh.infrastructure.persistence.export.pdf.PdfGenerator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// application/order/service/OrderApplicationService.java
@Service
@RequiredArgsConstructor
public class OrderApplicationService
    implements CreateOrderUseCase,
    AddItemToOrderUseCase,
    ConfirmOrderUseCase,
    CancelOrderUseCase,
    GetOrderUseCase,
    PrintOrderUseCase {

  private final OrderDomainRepository orderRepository;
  private final ProductDomainRepository productRepository;
  private final OrderApplicationMapper mapper;
  private final PdfGenerator pdfGenerator;

  @Override
  @Transactional
  public OrderDto execute(CreateOrderCommand command) {
    Order order = new Order(command.getCustomerId());
    Order saved = orderRepository.save(order);
    return mapper.toDto(saved);
  }

  @Override
  @Transactional
  public OrderDto execute(AddItemCommand command) {
    // Lock order để tránh concurrent modification
    Order order = orderRepository
        .findByIdForUpdate(command.getOrderId())
        .orElseThrow(() -> new NotFoundException(
            "Không tìm thấy order: " + command.getOrderId()
        ));

    // Lấy thông tin product — snapshot tại thời điểm đặt
    Product product = productRepository
        .findById(command.getProductId())
        .orElseThrow(() -> new NotFoundException(
            "Không tìm thấy sản phẩm: " + command.getProductId()
        ));

    if (!product.isInStock()) {
      throw new BusinessException("Sản phẩm đã hết hàng");
    }

    // Domain xử lý business rule — Aggregate Root là cửa duy nhất
    order.addItem(
        product.getId(),
        product.getName(),
        command.getQuantity(),
        new Money(product.getPrice().getValue())
    );

    return mapper.toDto(orderRepository.save(order));
  }

  @Override
  @Transactional
  public OrderDto execute(ConfirmOrderCommand command) {
    Order order = orderRepository
        .findByIdForUpdate(command.getOrderId())
        .orElseThrow(() -> new NotFoundException(
            "Không tìm thấy order: " + command.getOrderId()
        ));

    // Domain validate — confirm chỉ được khi DRAFT + có items
    order.confirm();

    // Giảm stock từng sản phẩm sau khi confirm
    // Dùng findByIdForUpdate để tránh race condition stock
    for (OrderItem item : order.getItems()) {
      Product product = productRepository
          .findByIdForUpdate(item.getProductId())
          .orElseThrow(() -> new NotFoundException(
              "Sản phẩm không tồn tại: " + item.getProductId()
          ));

      product.decreaseStock(item.getQuantity());
      productRepository.save(product);
    }

    return mapper.toDto(orderRepository.save(order));
  }

  @Override
  @Transactional
  public OrderDto execute(CancelOrderCommand command) {
    Order order = orderRepository
        .findByIdForUpdate(command.getOrderId())
        .orElseThrow(() -> new NotFoundException(
            "Không tìm thấy order: " + command.getOrderId()
        ));

    // Kiểm tra ownership — chỉ owner hoặc ADMIN mới cancel được
    // customerId null = ADMIN đang cancel
    if (command.getCustomerId() != null
        && !order.belongsTo(command.getCustomerId())) {
      throw new ForbiddenException(
          "Bạn không có quyền hủy đơn hàng này"
      );
    }

    // Domain trả về true nếu cần hoàn stock
    boolean needRestoreStock = order.cancel();

    if (needRestoreStock) {
      for (OrderItem item : order.getItems()) {
        Product product = productRepository
            .findByIdForUpdate(item.getProductId())
            .orElseThrow(() -> new NotFoundException(
                "Sản phẩm không tồn tại: " + item.getProductId()
            ));
        product.increaseStock(item.getQuantity());
        productRepository.save(product);
      }
    }

    return mapper.toDto(orderRepository.save(order));
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDto execute(String orderId, String customerId) {
    Order order = orderRepository
        .findById(orderId)
        .orElseThrow(() -> new NotFoundException(
            "Không tìm thấy order: " + orderId
        ));

    // customerId null = ADMIN xem tất cả
    if (customerId != null && !order.belongsTo(customerId)) {
      throw new ForbiddenException(
          "Bạn không có quyền xem đơn hàng này"
      );
    }

    return mapper.toDto(order);
  }

  @Override
  public byte[] execute(String orderId) {

    Order order =
        orderRepository.findById(orderId)
            .orElseThrow();

    OrderPrintDto dto =
        OrderPrintDto.builder()
            .orderCode(order.getId())
            .customerName(order.getCustomerId())
//            .phone(order.getPhone())
//            .address(order.getAddress())
            .totalAmount(order.getTotalAmount().getAmount())
            .items(
                order.getItems()
                    .stream()
                    .map(item ->
                        OrderItemPrintDto.builder()
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .price(new Money(item.getUnitPrice().getAmount()).getAmount())
                            .build())
                    .toList())
            .build();

    return pdfGenerator.generateOrderPdf(dto);
  }
}
