package com.example.qlbh.common.util;

import java.net.URL;
import java.util.Base64;

public final class TextAsBase64 {
    private TextAsBase64() {
    }

    public static String downloadQrAsBase64(String qrUrl) {

        try {

            byte[] bytes = new URL(qrUrl)
                    .openStream()
                    .readAllBytes();

            return Base64.getEncoder()
                    .encodeToString(bytes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
