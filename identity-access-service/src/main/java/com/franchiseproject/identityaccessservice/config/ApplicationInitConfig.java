package com.franchiseproject.identityaccessservice.config;

import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                Role role = roleRepository.findByName("admin");
                User user = User.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("sapassword"))
                        .role(role)
                        .email("hthu03.thu@gmail.com")
                        .fullName("Quản trị viên")
                        .status(UserStatus.ACTIVE)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: sapassword");
            }
        };
    }
}
