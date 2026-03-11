package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @GetMapping("/auth/token")
    public String generateToken() {
        return jwtService.generateToken("admin");
    }
}