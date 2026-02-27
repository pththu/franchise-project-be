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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
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

    @NonFinal
    @Value("${jwt.private-key}")
    private String PRIVATE_KEY;

    private JwtKeyProperties jwtKeyProperties;

    public AuthenticationResponse login(AuthenticationRequest request) throws Exception {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean result = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!result) throw new AppException(ErrorCode.UNAUTHORIZED);

        var token = generateToken(user.getUsername(), user.getRole().getName());

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
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
        RSAPrivateKey privateKey = jwtKeyProperties.getPrivateKeyObject();

        JWSVerifier verifier = new RSASSAVerifier(publicKey);

        boolean verified = signedJWT.verify(verifier);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        System.out.println("Verified: " + verified);
        System.out.println("Expiry: " + expiryTime);
        System.out.println("Now: " + new Date());
        System.out.println("Private modulus: " + privateKey.getModulus());
        System.out.println("Public modulus: " + publicKey.getModulus());

        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }

    private String generateToken(String username, String role) throws Exception {
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("scope", role)
                .issuer("identity-service")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(10, ChronoUnit.MINUTES).toEpochMilli()))
                .build();

        try {
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            JWSSigner signer = new RSASSASigner(jwtKeyProperties.getPrivateKeyObject());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new AppException(ErrorCode.CREATE_TOKEN_FAIL);
        }

    }

}
