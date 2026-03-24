package com.franchiseproject.customerservice.controller;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.request.CreateCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SyncCustomerRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerDetailResponse;
import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.CustomerResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import com.franchiseproject.customerservice.mapper.CustomerMapper;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import com.franchiseproject.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerController {
    CustomerService customerService;
    CustomerMapper customerMapper;

    @GetMapping("/get-all")
    public ApiResponse<List<CustomerResponse>> getAllCustomer(@PathParam("page") int page) {
        log.info("Start");
        return ApiResponse.<List<CustomerResponse>>builder()
                .statusCode(200)
                .message("Get all customer")
                .data(customerService.getAll(page))
                .build();
    }

    // Search and filter Customer
//    @GetMapping("/search")
//    public ApiResponse<PageResponse<CustomerFranchiseResponse>> searchCustomers(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) CustomerStatus status,
//            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        return ApiResponse.<PageResponse<CustomerFranchiseResponse>>builder()
//                .statusCode(200)
//                .message("Search customers successfully")
//                .data(customerService.searchCustomers(keyword, status, pageable))
//                .build();
//    }

    // View Loyalty tier and point
//    @GetMapping("/{id}")
//    public ApiResponse<CustomerDetailResponse> getCustomerDetail(@PathVariable UUID id) {
//        Customer customer = customerService.getCustomerById(id);
//        List<CustomerFranchise> loyaltyInfos = customerFranchiseService.getLoyaltyInfoByCustomerId(id);
//
//        CustomerDetailResponse detailResponse = CustomerDetailResponse.builder()
//                .customer(customerMapper.toCustomerResponse(customer))
//                .loyaltyInfos(customerMapper.toLoyaltyInfoResponseList(loyaltyInfos))
//                .build();
//
//        return ApiResponse.<CustomerDetailResponse>builder()
//                .statusCode(200)
//                .message("Get customer detail successfully")
//                .data(detailResponse)
//                .build();
//    }

//    @GetMapping("/all/search")
//    public ApiResponse<PageResponse<CustomerFranchiseResponse>> searchAllCustomers(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) CustomerStatus status,
//            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        return ApiResponse.<PageResponse<CustomerFranchiseResponse>>builder()
//                .statusCode(200)
//                .message("Search all customers successfully")
//                .data(customerService.searchAllCustomers(keyword, status, pageable))
//                .build();
//    }

    // Search (franchise)
//    @GetMapping("/franchise/search")
//    public ApiResponse<PageResponse<CustomerFranchiseResponse>> searchCustomersByFranchise(
//            // Ghi chú: franchiseId này thực tế nên lấy từ Context/JWT Token của nhân viên đăng nhập để bảo mật
//            @RequestParam UUID franchiseId,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) CustomerStatus status,
//            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        return ApiResponse.<PageResponse<CustomerFranchiseResponse>>builder()
//                .statusCode(200)
//                .message("Search customers by franchise successfully")
//                .data(customerService.searchCustomersByFranchise(franchiseId, keyword, status, pageable))
//                .build();
//    }

    // CREATE tại quầy
//    @PostMapping("/franchise")
//    public ApiResponse<CustomerFranchiseResponse> createCustomerAtFranchise(
//            @RequestParam UUID franchiseId, // Tương tự, ưu tiên lấy từ Token nhân viên
//            @Valid @RequestBody CreateCustomerRequest request) {
//        return ApiResponse.<CustomerFranchiseResponse>builder()
//                .statusCode(201)
//                .message("Create or link customer to franchise successfully")
//                .data(customerService.createOrLinkCustomerAtFranchise(request, franchiseId))
//                .build();
//    }

    // UPDATE khách hàng
//    @PutMapping("/{id}")
//    public ApiResponse<CustomerFranchiseResponse> updateCustomer(
//            @PathVariable UUID id,
//            @Valid @RequestBody UpdateCustomerRequest request) {
//        return ApiResponse.<CustomerFranchiseResponse>builder()
//                .statusCode(200)
//                .message("Update customer successfully")
//                .data(customerService.updateCustomer(id, request))
//                .build();
//    }

    // DELETE (Soft delete)
//    @DeleteMapping("/{id}")
//    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
//        customerService.deleteCustomer(id);
//        return ApiResponse.<Void>builder()
//                .statusCode(200)
//                .message("Delete customer successfully")
//                .build();
//    }

    // API NỘI BỘ: Để Identity Service gọi sang đồng bộ khi khách tự đăng ký App
//    @PostMapping("/internal/sync")
//    public ApiResponse<Void> syncCustomerFromIdentity(@RequestBody SyncCustomerRequest request) {
//        customerService.syncCustomerFromIdentity(request);
//        return ApiResponse.<Void>builder()
//                .statusCode(200)
//                .message("Sync customer successfully")
//                .build();
//    }

    // ================== READ ==================

    // 1. Dành cho Admin: Xem toàn bộ khách hàng trên hệ thống
    @GetMapping("/admin")
    public ApiResponse<PageResponse<CustomerFranchise>> getAllCustomersForAdmin(
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<PageResponse<CustomerFranchise>>builder()
                .statusCode(200)
                .message("Get all customers successfully")
                .data(customerService.getCustomersForAdmin(status, pageable))
                .build();
    }

    // 2. Dành cho Manager: Chỉ xem khách của Franchise
    @GetMapping("/franchise")
    public ApiResponse<PageResponse<CustomerFranchise>> getCustomersForManager(
            @RequestParam UUID franchiseId, // Trong thực tế, Extract UUID này từ JWT Token của Manager qua filter
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<PageResponse<CustomerFranchise>>builder()
                .statusCode(200)
                .message("Get franchise customers successfully")
                .data(customerService.getCustomersForManager(franchiseId, status, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerFranchise> getCustomerDetail(@PathVariable UUID id) {
        return ApiResponse.<CustomerFranchise>builder()
                .statusCode(200)
                .data(customerService.getCustomerById(id))
                .build();
    }

    // ================== CREATE / SYNC ==================

    // 3. Nhân viên tạo khách hàng tại quầy POS
    @PostMapping("/franchise/{franchiseId}")
    public ApiResponse<CustomerFranchise> createCustomerAtFranchise(
            @PathVariable UUID franchiseId,
            @RequestParam UUID customerId, // ID được Identity Service sinh ra khi đăng ký SĐT
            @RequestParam(required = false) CustomerType type) {
        return ApiResponse.<CustomerFranchise>builder()
                .statusCode(201)
                .message("Link customer to franchise successfully")
                .data(customerService.createCustomerAtFranchise(customerId, franchiseId, type))
                .build();
    }

    // 4. API Nội bộ: Identity Service gọi sang khi user tự tải App và đăng ký account
    @PostMapping("/internal/sync")
    public ApiResponse<Void> syncCustomerFromIdentity(
            @RequestParam UUID customerId,
            @RequestParam(required = false) CustomerType type) {
        customerService.syncCustomerFromIdentity(customerId, type);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Sync customer successfully")
                .build();
    }

    // ================== UPDATE & DELETE ==================

    // 5. Cập nhật trạng thái
    @PatchMapping("/{id}/status")
    public ApiResponse<CustomerFranchise> updateStatus(
            @PathVariable UUID id,
            @RequestParam CustomerStatus status) {
        return ApiResponse.<CustomerFranchise>builder()
                .statusCode(200)
                .message("Update customer status successfully")
                .data(customerService.updateCustomerStatus(id, status))
                .build();
    }

    // 6. Xóa tài khoản (Soft delete cho cả User tự xóa hoặc Staff xóa)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Delete customer successfully")
                .build();
    }
}