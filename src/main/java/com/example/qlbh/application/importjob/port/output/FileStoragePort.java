package com.example.qlbh.application.importjob.port.output;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {

  String upload(MultipartFile file);

  InputStream download(String objectName);
}
