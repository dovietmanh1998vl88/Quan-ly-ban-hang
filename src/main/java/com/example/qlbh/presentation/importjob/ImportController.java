package com.example.qlbh.presentation.importjob;

import com.example.qlbh.application.importjob.usecase.TriggerImportUseCase;
import com.example.qlbh.application.importjob.usecase.UploadImportFileUseCase;
import com.example.qlbh.common.response.BaseResponse;
import com.example.qlbh.presentation.importjob.response.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/imports")
@RequiredArgsConstructor
@Tag(name = "Import", description = "Upload và import file Excel vào hệ thống")
public class ImportController {

  private final UploadImportFileUseCase uploadUseCase;
  private final TriggerImportUseCase triggerImportUseCase;

  /**
   * Step 1: Upload file lên MinIO, nhận objectName. Tách upload và import → nếu import lỗi vẫn giữ được file để retry.
   */
  @PostMapping("/upload")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  @Operation(summary = "Upload file Excel lên MinIO")
  public BaseResponse<UploadResponse> upload(
      @RequestParam("file") MultipartFile file
  ) {
    String objectName = uploadUseCase.upload(file);
    return BaseResponse.success(
        "Upload thành công",
        new UploadResponse(objectName, file.getOriginalFilename(), file.getSize())
    );
  }

  /**
   * Step 2: Trigger import — đọc file từ MinIO, parse, save vào DB.
   */
  @PostMapping("/trigger")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  @Operation(summary = "Import file đã upload vào database")
  public BaseResponse<Void> triggerImport(
      @RequestParam("objectName") String objectName,
      @RequestParam("fileName") String fileName
  ) {
    triggerImportUseCase.triggerImport(objectName, fileName);
    return BaseResponse.success("Import thành công", null);
  }

  /**
   * Step 1+2 kết hợp: upload và import ngay lập tức. Dùng cho trường hợp đơn giản, file nhỏ.
   */
  @PostMapping("/upload-and-import")
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  @Operation(summary = "Upload và import ngay lập tức")
  public BaseResponse<Void> uploadAndImport(
      @RequestParam("file") MultipartFile file
  ) {
    String objectName = uploadUseCase.upload(file);
    triggerImportUseCase.triggerImport(objectName, file.getOriginalFilename());
    return BaseResponse.success("Upload và import thành công", null);
  }

}