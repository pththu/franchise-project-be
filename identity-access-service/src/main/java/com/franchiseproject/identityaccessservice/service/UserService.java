package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> getAll ();
    User getOne (UUID userId);

    User createOne (UserCreationRequest request);

}
