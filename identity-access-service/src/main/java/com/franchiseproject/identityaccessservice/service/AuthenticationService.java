package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.AuthenticationRequest;
import com.franchiseproject.identityaccessservice.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    boolean login(AuthenticationRequest request);
}
