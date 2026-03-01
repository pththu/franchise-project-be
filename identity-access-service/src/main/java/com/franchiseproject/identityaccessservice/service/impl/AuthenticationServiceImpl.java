package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.config.JwtKeyProperties;
import com.franchiseproject.identityaccessservice.dto.request.AuthenticationRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.IntrospectRequest;
import com.franchiseproject.identityaccessservice.dto.response.AuthenticationResponse;
import com.franchiseproject.identityaccessservice.dto.response.IntrospectResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import com.franchiseproject.identityaccessservice.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.crypto.impl.RSASSA;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@CommonsLog
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    private JwtKeyProperties jwtKeyProperties;

    @Override
    public boolean logout() {
        return true;
    }

    public AuthenticationResponse login(User user, HttpServletResponse response)
            throws Exception {
        var accessToken = generateAccessToken(user.getUsername(), user.getRole().getName());
        var refeshToken = generateRefreshToken(user.getUsername());

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refeshToken)
                .httpOnly(true)
                .secure(true)
                .path("/refresh")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .accessToken(accessToken)
                .build();
    }

    public User register(CustomerRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);


        User user = userMapper.toUser(request);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.builder()
                .id(UUID.fromString("591c1851-fb9b-40f0-86ae-d6b660101d2b"))
                .build());
        user.setVerifyEmail(false);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request)
            throws Exception {

        var token = request.getToken();

        SignedJWT signedJWT = SignedJWT.parse(token);

        // Lấy public key
        RSAPublicKey publicKey = jwtKeyProperties.getPublicKeyObject();

        JWSVerifier verifier = new RSASSAVerifier(publicKey);

        boolean verified = signedJWT.verify(verifier);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }

    private String generateAccessToken(String username, String role) throws Exception {
        Instant now = Instant.now();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("scope", role)
                .claim("type", "access")
                .issuer("identity-service")
                .issueTime(new Date())
                .expirationTime(new Date(now.plus(10, ChronoUnit.MINUTES).toEpochMilli()))
                .build();

        return signToken(jwtClaimsSet);
    }

    private String generateRefreshToken(String username) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("type", "refresh")
                .issuer("identity-service")
                .issueTime(new Date())
                .expirationTime(new Date(now.plus(14, ChronoUnit.DAYS).toEpochMilli()))
                .build();

        return signToken(jwtClaimsSet);
    }

    private String signToken(JWTClaimsSet claimsSet) {
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();
        try {
            SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
            JWSSigner signer = new RSASSASigner(jwtKeyProperties.getPrivateKeyObject());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new AppException(ErrorCode.CREATE_TOKEN_FAIL);
        }
    }
}
