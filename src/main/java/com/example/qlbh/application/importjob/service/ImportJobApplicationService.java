// application/importjob/service/ImportJobApplicationService.java
package com.example.qlbh.application.importjob.service;

import com.example.qlbh.application.importjob.port.output.FileStoragePort;
import com.example.qlbh.application.importjob.usecase.TriggerImportUseCase;
import com.example.qlbh.application.importjob.usecase.UploadImportFileUseCase;
import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.domain.product.model.Product;
import com.example.qlbh.domain.product.repository.ProductDomainRepository;
import com.example.qlbh.infrastructure.persistence.product.excel.ProductExcelImporter;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobApplicationService
    implements UploadImportFileUseCase, TriggerImportUseCase {

  private final FileStoragePort fileStoragePort;
  private final ProductExcelImporter productExcelImporter;
  private final ProductDomainRepository productRepository;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final List<String> ALLOWED_TYPES = List.of(
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.ms-excel"
  );

  @Override
  @Transactional
  public String upload(MultipartFile file) {
    validateFile(file);

    String objectName = fileStoragePort.upload(file);
    log.info("[Import] File uploaded to MinIO: {}", objectName);
    return objectName;
  }

  @Override
  @Transactional
  public void triggerImport(String objectName, String fileName) {
    log.info("[Import] Starting import for object: {}", objectName);

    try {
      // Download từ MinIO
      InputStream inputStream = fileStoragePort.download(objectName);

      // Parse Excel → List<Product>
      List<Product> products = productExcelImporter.readFromStream(inputStream);
      log.info("[Import] Parsed {} products from Excel", products.size());

      // Save vào DB
      productRepository.saveAll(products);
      log.info("[Import] Saved {} products to DB", products.size());

    } catch (BusinessException e) {
      throw e; // re-throw business exception
    } catch (Exception e) {
      log.error("[Import] Import failed: {}", e.getMessage(), e);
      throw new BusinessException("Import thất bại: " + e.getMessage());
    }
  }

  // ===== Private helpers =====

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException("File không được trống");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BusinessException("File quá lớn. Tối đa 10MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new BusinessException(
          "Chỉ chấp nhận file Excel (.xlsx, .xls). ContentType nhận được: " + contentType
      );
    }
  }
}