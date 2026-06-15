package com.franchiseproject.paymentservice.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "momo")
@Component
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoProperties {
    String partner_code; //MOMO
    String return_url; //http://localhost:3000
    String end_point; //https://test-payment.momo.vn/v2/gateway/api
    String ipn_url; //http://localhost:8080/api/momo/ipn-handler
    String access_key; //F8BBA842ECF85
    String secret_key; //K951B6PE1waDMi640xX08PD3vg6EkVlz
    String request_type; //captureWallet

}
