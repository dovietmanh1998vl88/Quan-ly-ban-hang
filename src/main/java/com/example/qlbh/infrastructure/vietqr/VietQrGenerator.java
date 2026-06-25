package com.example.qlbh.infrastructure.vietqr;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VietQrGenerator {

        private final BankProperties bankProperties;

        public String generate(
                        String paymentCode,
                        BigDecimal amount) {
                String accountName = URLEncoder.encode(
                                bankProperties.getAccountName(),
                                StandardCharsets.UTF_8);
                return String.format(
                                "https://img.vietqr.io/image/%s-%s-compact2.png" +
                                                "?amount=%s" +
                                                "&addInfo=%s" +
                                                "&accountName=%s",
                                bankProperties.getCode(),
                                bankProperties.getAccountNumber(),
                                amount,
                                paymentCode,
                                accountName);
        }
}
