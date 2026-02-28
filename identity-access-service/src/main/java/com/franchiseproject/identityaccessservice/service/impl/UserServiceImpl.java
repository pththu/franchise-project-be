package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserUpdateRequest;
import com.franchiseproject.identityaccessservice.dto.response.ChangePasswordResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserDeleteResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserUpdateResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import com.franchiseproject.identityaccessservice.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse getById(UUID userId) {
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    @Override
    public User createOne(UserCreationRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setStatus(UserStatus.ACTIVE);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        return userRepository.save(user);
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        System.out.println("User: " + user.getUsername());
        System.out.println("Matches: " + passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash()));
        if (passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public UserResponse getProfile(String username) {
        return userMapper.toUserResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)));
    }

    @Override
    public UserUpdateResponse updateAccountInfomation(String username, UserUpdateRequest request) {

        log.info("request" +request.getFullName());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String fullName = request.getFullName();
        String phone = request.getPhone();
        String gender = request.getGender();

        if (fullName != null && phone != null && gender != null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        if (fullName != null && !fullName.isEmpty() && !user.getFullName().equals(fullName)) {
            user.setFullName(fullName);
            log.info("fullname 1");
        }
        if (phone != null && !phone.isEmpty() && !user.getPhone().equals(phone)) {
            user.setPhone(phone);
            log.info("2");
        }
        if (gender != null && user.isGender() != Boolean.getBoolean(gender)) {
            log.info("3");
            user.setGender(Boolean.getBoolean(gender));
        }

        log.info("user :" + user.getFullName());
        userRepository.save(user);

        return UserUpdateResponse.builder()
                .isUpdated(true)
                .userResponse(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public UserDeleteResponse deleteAccountUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        return UserDeleteResponse.builder()
                .isDeleted(true)
                .build();
    }
}
