package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.client.FranchiseClient;
import com.franchiseproject.identityaccessservice.dto.request.*;
import com.franchiseproject.identityaccessservice.dto.response.*;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.FranchiseStatus;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import com.franchiseproject.identityaccessservice.service.CognitoService;
import com.franchiseproject.identityaccessservice.service.UserService;
import jakarta.transaction.Transactional;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {

    CognitoService cognitoService;
    RoleRepository roleRepository;
    UserRepository userRepository;
    UserMapper userMapper;
    FranchiseClient franchiseClient;
    PasswordEncoder passwordEncoder;

    static final int DEFAULT_PAGE_SIZE = 20;

    @Override
    public User getById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
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

    @Override
    public Page<UserResponse> search(SeachUsersRequest request) {

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

        Pageable pageable = PageRequest.of(
                request.getPage().intValue(),
                request.getSize().intValue(),
                sort
        );

        Page<User> usersPage = userRepository.searchUsers(
                keyword,
                roleName,
                status,
                request.getGender(),
                pageable
        );

        if (usersPage.isEmpty()) {
            return Page.empty(usersPage.getPageable());
        }

        Set<UUID> franchiseIds = usersPage.getContent().stream()
                .map(User::getFranchiseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.info("franchiseIds {}", franchiseIds);

        Map<UUID, FranchiseResponse> franchiseMap = fetchFranchisesConcurrently(franchiseIds);

        return usersPage.map(user -> {
            UserResponse response = userMapper.toUserResponse(user, franchiseClient);

            if (user.getFranchiseId() != null) {
                response.setFranchise(franchiseMap.get(user.getFranchiseId()));
            }

            return response;
        });
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

    /**
     * Admin/Manager tạo user thủ công.
     * Sau khi tạo trên Cognito + lưu DB → add ngay vào Cognito group theo roleName.
     * (Không cần qua flow verify vì admin tạo, email verify sẽ được admin xử lý riêng)
     */
    @Override
    @Transactional
    public UserCreationResponse createOne(UserCreationRequest req, Role role) {

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_IS_EXISTS);
        }

        String passwordDefault = "Franchise@01";
        String cognitoSub;

        try {
            cognitoSub = cognitoService.registerUser(
                    req.getUsername(),
                    passwordDefault,
                    req.getEmail(),
                    req.getFullName(),
                    req.getPhone()
            );
            log.info("cognitoSub: {}", cognitoSub);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("CreateUser failed at Cognito registration", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        UUID assignedFranchiseId = req.getFranchiseId();
        if (role.getName().equalsIgnoreCase("CUSTOMER")) {
            assignedFranchiseId = null;
        }

        User user = User.builder()
                .id(UUID.fromString(cognitoSub))
                .username(req.getUsername())
                .email(req.getEmail())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .verifyEmail(false)
                .gender(req.isGender())
                .role(role)
                .franchiseId(assignedFranchiseId)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        try {
            cognitoService.addUserToGroup(user.getUsername(), role.getName());
            log.info("CreateUser: added {} to Cognito group '{}'", user.getUsername(), role.getName());
        } catch (Exception e) {
            log.warn("CreateUser: failed to add {} to Cognito group '{}': {}",
                    user.getUsername(), role.getName(), e.getMessage());
        }

        return UserCreationResponse.builder()
                .isCreated(true)
                .userResponse(userMapper.toUserResponse(user, franchiseClient))
                .build();
    }

    /**
     * Assign role mới cho user:
     * 1. Xóa user khỏi Cognito group cũ
     * 2. Add user vào Cognito group mới
     * 3. Cập nhật role trong DB
     */
    @Override
    @Transactional
    public AssignRoleResponse assignRole(Role newRole, User user) {
        if (newRole == null || user == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        String oldRoleName = user.getRole() != null ? user.getRole().getName() : null;
        String newRoleName = newRole.getName();

        if (newRoleName.equals(oldRoleName)) {
            log.info("AssignRole: user {} already has role '{}', skipping", user.getUsername(), newRoleName);
            return AssignRoleResponse.builder()
                    .isAssigned(true)
                    .build();
        }

        if (oldRoleName != null) {
            try {
                cognitoService.removeUserFromGroup(user.getUsername(), oldRoleName);
                log.info("AssignRole: removed {} from Cognito group '{}'", user.getUsername(), oldRoleName);
            } catch (Exception e) {
                log.warn("AssignRole: could not remove {} from old Cognito group '{}': {}",
                        user.getUsername(), oldRoleName, e.getMessage());
            }
        }

        try {
            cognitoService.addUserToGroup(user.getUsername(), newRoleName);
            log.info("AssignRole: added {} to Cognito group '{}'", user.getUsername(), newRoleName);
        } catch (Exception e) {
            log.error("AssignRole: failed to add {} to Cognito group '{}': {}",
                    user.getUsername(), newRoleName, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        user.setRole(newRole);
        userRepository.save(user);

        return AssignRoleResponse.builder()
                .isAssigned(true)
                .build();
    }

    /**
     * Update trạng thái user (ACTIVE / SUSPENDED / DELETED).
     * Chỉ lưu xuống DB — không cần tác động Cognito vì login đã check status.
     */
    @Override
    @Transactional
    public UserStatusResponse updateStatus(UUID userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        log.info("UpdateStatus: user={}, {} → {}", user.getUsername(), user.getStatus(), newStatus);
        user.setStatus(newStatus);
        userRepository.save(user);

        return UserStatusResponse.builder()
                .userId(userId)
                .status(newStatus)
                .isUpdated(true)
                .build();
    }

    @Override
    @Transactional
    public UserDeleteResponse deleteAccountUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("DeleteAccount: status=DELETED saved for user={}", user.getUsername());

        try {
            cognitoService.disableUser(user.getUsername());
        } catch (Exception e) {
            log.warn("DeleteAccount: failed to disable user {} in Cognito: {}",
                    user.getUsername(), e.getMessage());
        }

        return UserDeleteResponse.builder()
                .isDeleted(true)
                .build();
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public UserResponse getProfile(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

//        UUID franchiseId = user.getFranchiseId();
//        UserResponse userResponse = userMapper.toUserResponse(user, franchiseClient);
//        if (franchiseId != null) {
//            FranchiseResponse franchiseResponse = franchiseClient.getFranchiseById(franchiseId);
//            if (franchiseResponse != null) {
//                userResponse.setFranchise(franchiseResponse);
//            }
//        }

        return userMapper.toUserResponse(user, franchiseClient);
    }

    @Override
    public UserUpdateResponse updateProfile(UUID subject, UpdateProfileRequest request) {

        User user = userRepository.findById(subject)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean changed = false;

        String fullName = request.getFullName();
        if (fullName != null && !fullName.isBlank() && !user.getFullName().equals(fullName)) {
            user.setFullName(fullName);
            changed = true;
        }

        if (request.getGender() != null && request.getGender().booleanValue() != user.isGender()) {
            user.setGender(request.getGender().booleanValue());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }

        return UserUpdateResponse.builder()
                .isUpdated(changed)
                .userResponse(userMapper.toUserResponse(user, franchiseClient))
                .build();
    }

    /**
     * Cập nhật thông tin cá nhân (fullName, phone, gender).
     * Chỉ update field nào có giá trị mới khác giá trị hiện tại.
     * <p>
     * subject: JWT sub — có thể là UUID (từ Cognito access token) hoặc username.
     */
    @Override
    @Transactional
    public UserUpdateResponse updateAccountInformation(UUID userId, UserUpdateRequest request) {
        log.info("UpdateAccountInformation: subject={}, request={}", userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean changed = false;

        String fullName = request.getFullName();
        if (fullName != null && !fullName.isBlank() && !fullName.equals(user.getFullName())) {
            user.setFullName(fullName);
            changed = true;
            log.info("UpdateAccountInformation: updated fullName for {}", user.getUsername());
        }

        if (request.getStatus() == UserStatus.DELETED) {
            changed = deleteAccountUser(userId).isDeleted();
        }

        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

            changed = assignRole(role, user).isAssigned();
        }


        if (request.getFranchise() != null) {

            UUID franchiseId = request.getFranchise();
            CheckFranchiseResponse response = franchiseClient.checkFranchiseById(franchiseId);

            log.info("response: {} {}",response.getIsExists(), response.getStatus());

            if (!response.getIsExists().booleanValue()) {
                throw new AppException(ErrorCode.FRANCHISE_NOT_EXISTED);
            }

            if (response.getStatus() == FranchiseStatus.INACTIVE) {
                throw new AppException(ErrorCode.FRANCHISE_INACTIVE);
            }

            user.setFranchiseId(franchiseId);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
//
//        UUID franchiseId = user.getFranchiseId();
//        UserResponse userResponse = userMapper.toUserResponse(user, franchiseClient);
//        if (franchiseId != null) {
//            FranchiseResponse franchiseResponse = franchiseClient.getFranchiseById(franchiseId);
//            if (franchiseResponse != null) {
//                userResponse.setFranchise(franchiseResponse);
//            }
//        }

        return UserUpdateResponse.builder()
                .isUpdated(changed)
                .userResponse(userMapper.toUserResponse(user, franchiseClient))
                .build();
    }

    @Override
    public List<User> getUsersByIds(List<UUID> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public Page<UserResponse> getStaffByFranchise(UUID franchiseId, int page) {

        CheckFranchiseResponse response = franchiseClient.checkFranchiseById(franchiseId);

        log.info("response: {} {}",response.getIsExists(), response.getStatus());

        if (!response.getIsExists().booleanValue()) {
            throw new AppException(ErrorCode.FRANCHISE_NOT_EXISTED);
        }

        if (response.getStatus() == FranchiseStatus.INACTIVE) {
            throw new AppException(ErrorCode.FRANCHISE_INACTIVE);
        }

        Sort sort =  Sort.by("username").ascending();

        Pageable pageable = PageRequest.of(page,10, sort);
        Page<User> users = userRepository.findByRole("STAFF", franchiseId, pageable);

        return users.map(user -> userMapper.toUserResponse(user, franchiseClient));
    }

    private Map<UUID, FranchiseResponse> fetchFranchisesConcurrently(Set<UUID> franchiseIds) {
        if (franchiseIds.isEmpty()) return Collections.emptyMap();

        List<CompletableFuture<AbstractMap.SimpleEntry<UUID, FranchiseResponse>>> futures = franchiseIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    FranchiseResponse franchise = franchiseClient.getFranchiseById(id);
                    return new AbstractMap.SimpleEntry<>(id, franchise);
                }))
                .toList();
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user, franchiseClient);
    }
}
