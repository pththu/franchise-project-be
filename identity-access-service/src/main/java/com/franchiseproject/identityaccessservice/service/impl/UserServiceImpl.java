package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.SeachUsersRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserUpdateRequest;
import com.franchiseproject.identityaccessservice.dto.response.*;
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
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    static final int DEFAULT_PAGE_SIZE = 20;

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
    public User getById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserCreationResponse createOne(User user) {
        userRepository.save(user);
        return UserCreationResponse.builder()
                .isCreated(true)
                .userResponse(userMapper.toUserResponse(user))
                .build();
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
    public UserResponse getProfile(UUID userId) {
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)));
    }

    @Override
    public UserUpdateResponse updateAccountInfomation(String username, UserUpdateRequest request) {

        log.info("request" + request.getFullName());
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

    @Override
    public AssignRoleResponse assignRole(Role role, User user) {
        if (role == null || user == null) throw new AppException(ErrorCode.DATA_IS_NULL);
        user.setRole(role);
        userRepository.save(user);
        return AssignRoleResponse.builder()
                .isAssigned(true)
                .build();
    }

    @Override
    public Page<User> getAll(int page) {
        Pageable pageable = PageRequest.of(
                page,
                DEFAULT_PAGE_SIZE,
                Sort.by("username").ascending()
        );
        return userRepository.findAll(pageable);
    }

    //    @Override
//    public Page<User> search(
//            String keyword,
//            String role,
//            UserStatus status,
//            Boolean gender,
//            int page,
//            int size,
//            String sortBy,
//            String sortDir
//    ) {
//       return null;
//    }
    @Override
    public Page<User> search(SeachUsersRequest request) {

        String keyword = (request.getKeyword() != null && !request.getKeyword().trim().isEmpty())
                ? request.getKeyword().trim() : null;

        String roleName = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim() : null;

        String sortProperty = request.getSortBy();
        if ("name".equalsIgnoreCase(sortProperty)) {
            sortProperty = "fullName";
        }

        UserStatus status = (request.getStatus() != null && !request.getStatus().trim().isEmpty())
                ? UserStatus.valueOf(request.getStatus().trim()) : null;

        Sort sort = request.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortProperty).ascending()
                : Sort.by(sortProperty).descending();


        Pageable pageable = PageRequest.of(request.getPage().intValue(), request.getSize().intValue(), sort);

        return userRepository.searchUsers(
                keyword,
                roleName,
                status,
                request.getGender(),
                pageable
        );
    }

    @Override
    public StatsCountUserResponse countUsers() {
        Object[] result = (Object[]) userRepository.countUserStats()[0];

        return StatsCountUserResponse.builder()
                .totals(((Long) result[0]).intValue())
                .totalIsActive(((Long) result[1]).intValue())
                .totalIsSuspended(((Long) result[2]).intValue())
                .totalIsDeleted(((Long) result[3]).intValue())
                .totalAdmin(((Long) result[4]).intValue())
                .totalManager(((Long) result[5]).intValue())
                .totalStaff(((Long) result[6]).intValue())
                .totalCustomer(((Long) result[7]).intValue())
                .build();
    }
}
