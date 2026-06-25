package com.example.qlbh.infrastructure.vietqr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "payment.bank")
@Getter
@Setter
public class BankProperties {

    private String code;
    private String accountNumber;
    private String accountName;

}
