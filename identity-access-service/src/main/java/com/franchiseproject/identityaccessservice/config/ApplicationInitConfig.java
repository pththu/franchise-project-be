package com.franchiseproject.identityaccessservice.config;

import com.franchiseproject.identityaccessservice.dto.request.UserRegisterRequest;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import com.franchiseproject.identityaccessservice.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    AuthenticationService authenticationService;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
//            if (userRepository.findByUsername("admin").isEmpty()) {
//                UserRegisterRequest admin = UserRegisterRequest.builder()
//                        .username("admin")
//                        .roleName("ADMIN")
//                        .email("hthu03.thu@gmail.com")
//                        .password("Franchise@03")
//                        .phone("0333411964")
//                        .fullName("Quản trị viên")
//                        .gender(true)
//                        .build();
//
//                authenticationService.register(admin);
//                log.warn("admin user has been created with default password: sapassword");
//            }

//            if (userRepository.findByUsername("manager").isEmpty()) {
//
//                UserRegisterRequest manager = UserRegisterRequest.builder()
//                        .username("manager")
//                        .roleName("MANAGER")
//                        .email("manager.demo@gmail.com")
//                        .password("Franchise@03")
//                        .phone("0787655428")
//                        .fullName("Nguoừi quản lý 1")
//                        .gender(true)
//                        .build();
//
//                UserRegisterRequest staff = UserRegisterRequest.builder()
//                        .username("staff")
//                        .roleName("STAFF")
//                        .email("huynhthu280603@gmail.com")
//                        .password("Franchise@03")
//                        .phone("0986787954")
//                        .fullName("Nhân viên 1")
//                        .gender(true)
//                        .build();
//
//                authenticationService.register(manager);
//                authenticationService.register(staff);
//                log.warn("admin user has been created with default password: sapassword");
//            }


//            if (userRepository.findByUsername("staff").isEmpty()) {
//
//                UserRegisterRequest staff = UserRegisterRequest.builder()
//                        .username("staff")
//                        .roleName("STAFF")
//                        .email("huynhthu280603@gmail.com")
//                        .password("Franchise@03")
//                        .phone("0986787954")
//                        .fullName("Nhân viên 1")
//                        .gender(true)
//                        .build();
//
//                authenticationService.register(staff);
//                log.warn("admin user has been created with default password: sapassword");
//            }
        };
    }
}
