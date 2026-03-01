package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.AuthenticationRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.IntrospectRequest;
import com.franchiseproject.identityaccessservice.dto.response.AuthenticationResponse;
import com.franchiseproject.identityaccessservice.dto.response.IntrospectResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserLockResponse;
import com.franchiseproject.identityaccessservice.entity.User;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;
import java.util.UUID;

public interface AuthenticationService {
    boolean logout ();
    AuthenticationResponse login(AuthenticationRequest request, HttpServletResponse response) throws Exception;
    User register(CustomerRegisterRequest request);
    IntrospectResponse introspect(IntrospectRequest request) throws Exception;
    UserLockResponse lockUser(UUID userId);
}
