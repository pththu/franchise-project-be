//package com.franchiseproject.paymentservice.config;
//
//import com.franchiseproject.paymentservice.entity.PaymentMethod;
//import com.franchiseproject.paymentservice.repository.PaymentMethodRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class DataInitializer {
//
//    private final PaymentMethodRepository paymentMethodRepository;
//
//    @Bean
//    @Transactional
//    public CommandLineRunner initDatabase() {
//        return args -> {
//
//            if (paymentMethodRepository.count() == 0) {
//
//                PaymentMethod momo = PaymentMethod.builder()
//                        .methodName("MOMO")
//                        .provider("MoMo E-Wallet")
//                        .active(true)
//                        .build();
//
//                PaymentMethod cod = PaymentMethod.builder()
//                        .methodName("COD")
//                        .provider("Cash On Delivery")
//                        .active(true)
//                        .build();
//
//                PaymentMethod vnpay = PaymentMethod.builder()
//                        .methodName("VNPAY")
//                        .provider("VNPay Gateway")
//                        .active(true)
//                        .build();
//
//                paymentMethodRepository.saveAll(
//                        List.of(momo, cod, vnpay)
//                );
//                log.info("Payment methods initialized");
//            }
//        };
//    }
//}
