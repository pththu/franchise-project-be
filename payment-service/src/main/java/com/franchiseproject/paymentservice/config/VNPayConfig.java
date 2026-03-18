package com.franchiseproject.paymentservice.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class VNPayConfig {
    String tmnCode;
    String hashSecret;
    String paymentUrl;
    String returnUrl;
    String ipnUrl;
    String version;
    String command;
    String currencyCode;
    String locale;
}
