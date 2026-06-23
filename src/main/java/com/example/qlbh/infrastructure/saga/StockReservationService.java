package com.example.qlbh.infrastructure.saga;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.qlbh.domain.product.event.ProductStockUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationService {

  private final com.example.qlbh.domain.product.repository.ProductDomainRepository productRepository;

  /**
   * Reserve stock for an order item.
   * Throws exception if not enough stock.
   */
  public ProductStockUpdatedEvent reserve(
      String productId,
      int quantity,
      String orderId) {
    var product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));

    if (product.GetIntStock() < quantity) {
      throw new InsufficientStockException(
          "Product " + productId + " has insufficient stock");
    }

    int oldStock = product.GetIntStock();
    product.decreaseStock(quantity);
    productRepository.save(product);

    log.info("Stock reserved: productId={}, quantity={}, orderId={}",
        productId, quantity, orderId);

    return new ProductStockUpdatedEvent(
        productId,
        oldStock,
        product.GetIntStock(),
        quantity,
        "RESERVED",
        orderId,
        Instant.now());
  }

  /**
   * Release previously reserved stock (compensation).
   */
  public void release(String productId, int quantity, String orderId) {
    var product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));

    product.decreaseStock(quantity);
    productRepository.save(product);

    log.warn("Stock released (compensation): productId={}, quantity={}, orderId={}",
        productId, quantity, orderId);
  }

  public static class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
      super("Product not found: " + productId);
    }
  }

  public static class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
      super(message);
    }
  }
}
