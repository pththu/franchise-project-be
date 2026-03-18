package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.entity.User;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface GoogleOAuthService {
    TokenResponse exchangeCodeForTokens(String code);
}
