package com.example.qlbh.infrastructure.persistence.oder.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Service
public class QrCodeService {

    public byte[] generate(String content) {

        BitMatrix matrix = new MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                300,
                300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        return out.toByteArray();
    }
}