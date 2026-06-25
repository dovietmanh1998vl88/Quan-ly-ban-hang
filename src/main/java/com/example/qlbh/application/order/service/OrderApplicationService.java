package com.example.qlbh.application.order.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbh.application.order.command.AddItemCommand;
import com.example.qlbh.application.order.command.CancelOrderCommand;
import com.example.qlbh.application.order.command.ConfirmOrderCommand;
import com.example.qlbh.application.order.command.CreateOrderCommand;
import com.example.qlbh.application.order.dto.OrderDto;
import com.example.qlbh.application.order.dto.OrderItemPrintDto;
import com.example.qlbh.application.order.dto.OrderPrintDto;
import com.example.qlbh.application.order.dto.OrderRevenueReportDto;
import com.example.qlbh.application.order.mapper.OrderApplicationMapper;
import com.example.qlbh.application.order.port.PdfGenerator;
import com.example.qlbh.application.order.usecase.AddItemToOrderUseCase;
import com.example.qlbh.application.order.usecase.CancelOrderUseCase;
import com.example.qlbh.application.order.usecase.ConfirmOrderUseCase;
import com.example.qlbh.application.order.usecase.CreateOrderUseCase;
import com.example.qlbh.application.order.usecase.GetOrderUseCase;
import com.example.qlbh.application.order.usecase.PrintOrderUseCase;
import com.example.qlbh.application.order.usecase.RevenueReportUseCase;
import com.example.qlbh.common.enums.OrderStatus;
import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.common.exception.ForbiddenException;
import com.example.qlbh.common.exception.NotFoundException;
import com.example.qlbh.domain.order.model.Order;
import com.example.qlbh.domain.order.model.OrderItem;
import com.example.qlbh.domain.order.repository.OrderDomainRepository;
import com.example.qlbh.domain.order.service.OrderCodeGenerator;
import com.example.qlbh.domain.order.valueobject.Money;
import com.example.qlbh.domain.order.valueobject.OrderCode;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.repository.ProductDomainRepository;
import com.example.qlbh.infrastructure.vietqr.VietQrGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderApplicationService
                implements CreateOrderUseCase,
                AddItemToOrderUseCase,
                ConfirmOrderUseCase,
                CancelOrderUseCase,
                GetOrderUseCase,
                PrintOrderUseCase,
                RevenueReportUseCase {
        private final OrderDomainRepository orderRepository;
        private final ProductDomainRepository productRepository;
        private final OrderApplicationMapper mapper;
        private final PdfGenerator pdfGenerator;
        private final OrderCodeGenerator orderCodeGenerator;
        private final VietQrGenerator vietQrGenerator;

        @Override
        @Transactional
        public OrderDto execute(CreateOrderCommand command) {
                OrderCode orderCode = orderCodeGenerator.next();
                System.out.println("orderCode=" + orderCode.value());
                Order order = new Order(command.getCustomerId(), orderCode.value());
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
                                                "Không tìm thấy order: " + command.getOrderId()));

                // Lấy thông tin product — snapshot tại thời điểm đặt
                Product product = productRepository
                                .findById(command.getProductId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Không tìm thấy sản phẩm: " + command.getProductId()));

                if (!product.isInStock()) {
                        throw new BusinessException("Sản phẩm đã hết hàng");
                }

                // Domain xử lý business rule — Aggregate Root là cửa duy nhất
                order.addItem(
                                product.getId(),
                                product.getName(),
                                command.getQuantity(),
                                new Money(product.getPrice().getValue()));

                return mapper.toDto(orderRepository.save(order));
        }

        @Override
        @Transactional
        public OrderDto execute(ConfirmOrderCommand command) {
                Order order = orderRepository
                                .findByIdForUpdate(command.getOrderId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Không tìm thấy order: " + command.getOrderId()));
                List<OrderItem> sortedItems = order.getItems()
                                .stream()
                                .sorted(
                                                Comparator.comparing(OrderItem::getProductId))
                                .toList();

                // Giảm stock từng sản phẩm sau khi confirm
                // Dùng findByIdForUpdate để tránh race condition stock
                for (OrderItem item : sortedItems) {
                        Product product = productRepository
                                        .findByIdForUpdate(item.getProductId())
                                        .orElseThrow(() -> new NotFoundException(
                                                        "Sản phẩm không tồn tại: " + item.getProductId()));

                        product.decreaseStock(item.getQuantity());
                        productRepository.save(product);
                }
                // Domain validate — confirm chỉ được khi DRAFT + có items
                String qrUrl = vietQrGenerator.generate(
                                order.getId(),
                                order.getTotalAmount().getAmount());
                order.confirm(qrUrl);
                return mapper.toDto(orderRepository.save(order));
        }

        @Override
        @Transactional
        public OrderDto execute(CancelOrderCommand command) {
                Order order = orderRepository
                                .findByIdForUpdate(command.getOrderId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Không tìm thấy order: " + command.getOrderId()));

                // Kiểm tra ownership — chỉ owner hoặc ADMIN mới cancel được
                // customerId null = ADMIN đang cancel
                if (command.getCustomerId() != null
                                && !order.belongsTo(command.getCustomerId())) {
                        throw new ForbiddenException(
                                        "Bạn không có quyền hủy đơn hàng này");
                }

                // Domain trả về true nếu cần hoàn stock
                boolean needRestoreStock = order.cancel();

                if (needRestoreStock) {
                        List<OrderItem> sortedItems = order.getItems()
                                        .stream()
                                        .sorted(
                                                        Comparator.comparing(OrderItem::getProductId))
                                        .toList();
                        for (OrderItem item : sortedItems) {
                                Product product = productRepository
                                                .findByIdForUpdate(item.getProductId())
                                                .orElseThrow(() -> new NotFoundException(
                                                                "Sản phẩm không tồn tại: " + item.getProductId()));
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
                                                "Không tìm thấy order: " + orderId));

                // customerId null = ADMIN xem tất cả
                if (customerId != null && !order.belongsTo(customerId)) {
                        throw new ForbiddenException(
                                        "Bạn không có quyền xem đơn hàng này");
                }

                return mapper.toDto(order);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] execute(String orderId) {

                Order order = orderRepository.findById(orderId)
                                .orElseThrow();

                OrderPrintDto dto = OrderPrintDto.builder()
                                .orderCode(order.getId())
                                .customerName(order.getCustomerId())
                                .qrUrl(order.getQrUrl())
                                // .phone(order.getPhone())
                                // .address(order.getAddress())
                                .totalAmount(order.getTotalAmount().getAmount())
                                .items(
                                                order.getItems()
                                                                .stream()
                                                                .map(item -> OrderItemPrintDto.builder()
                                                                                .productName(item.getProductName())
                                                                                .quantity(item.getQuantity())
                                                                                .price(new Money(item.getUnitPrice()
                                                                                                .getAmount())
                                                                                                .getAmount())
                                                                                .build())
                                                                .toList())
                                .build();

                return pdfGenerator.generateOrderPdf(dto);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] execute(OrderRevenueReportDto command) {
                List<Order> orders = orderRepository.findByCreatedAtBetween(
                                command.getTungay(),
                                command.getDenngay());
                Money totalAmount = orders.stream()
                                .map(Order::getTotalAmount)
                                .reduce(Money.ZERO, Money::add);

                OrderRevenueReportDto dto = OrderRevenueReportDto.builder()
                                .tungay(command.getTungay())
                                .denngay(command.getDenngay())
                                .totalOder(orders.size())
                                .totalCancelledDOder(
                                                (int) orders.stream()
                                                                .filter(o -> o.getStatus()
                                                                                .equals(OrderStatus.CANCELLED))
                                                                .count())
                                .totalFinishOder((int) orders.stream()
                                                .filter(o -> o.getStatus().equals(OrderStatus.CONFIRMED)).count())
                                .totalAmount(totalAmount.getAmount())
                                .exportDate(java.time.LocalDateTime.now()
                                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                                .build();
                return pdfGenerator.PdfOrderRevenueGenerator(dto);
        }
}
