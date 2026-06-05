package com.example.qlbh.presentation.importjob.response;

public record UploadResponse(
    String objectName,
    String fileName,
    long fileSize
) {

}