package com.example.qlbh.application.importjob.port.output;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

// Port (interface) nằm trong Application layer
// → Application không biết MinIO, chỉ biết interface này
public interface FileStoragePort {

  String upload(MultipartFile file);       // trả về objectName (key trong MinIO)

  InputStream download(String objectName); // dùng khi cần đọc file để import
}
