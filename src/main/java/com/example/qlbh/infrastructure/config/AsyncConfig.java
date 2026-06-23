package com.example.qlbh.infrastructure.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * AsyncConfig — cấu hình thread pool cho @Async.
 * <p>
 * Tại sao cần thread pool riêng cho audit? → Tránh audit tasks chiếm thread pool mặc định của app → Có thể tune riêng
 * (size, queue capacity) mà không ảnh hưởng nghiệp vụ chính → Khi thread pool audit đầy → log lỗi, không block business
 * request
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

  /**
   * Thread pool riêng cho audit event listener. Bean name "auditExecutor" được dùng trong @Async("auditExecutor") nếu
   * muốn chỉ định rõ ràng.
   */
  @Bean(name = "auditExecutor")
  public Executor auditExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Core threads luôn sống — xử lý audit bình thường
    executor.setCorePoolSize(2);

    // Tối đa 5 threads khi audit burst (nhiều request cùng lúc)
    executor.setMaxPoolSize(5);

    // Queue 100 tasks trước khi tạo thêm thread
    executor.setQueueCapacity(100);

    executor.setThreadNamePrefix("audit-");

    // Khi shutdown app: chờ tối đa 30s để drain queue
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();
    return executor;
  }
}