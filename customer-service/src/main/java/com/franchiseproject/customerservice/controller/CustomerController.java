package com.franchiseproject.customerservice.controller;

import com.franchiseproject.customerservice.dto.ApiResponse;
import com.franchiseproject.customerservice.dto.request.SaveCustomerRequest;
import com.franchiseproject.customerservice.dto.request.SearchRequest;
import com.franchiseproject.customerservice.dto.request.UpdateCustomerRequest;
import com.franchiseproject.customerservice.dto.response.CustomerFranchiseResponse;
import com.franchiseproject.customerservice.dto.response.CustomerSummaryResponse;
import com.franchiseproject.customerservice.dto.response.PageResponse;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import com.franchiseproject.customerservice.entity.CustomerFranchise;
import com.franchiseproject.customerservice.exception.AppException;
import com.franchiseproject.customerservice.exception.ErrorCode;
import com.franchiseproject.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerController {

    CustomerService customerService;

    @GetMapping("/get-all")
    public ApiResponse<List<CustomerFranchiseResponse>> getAllCustomer(@PathParam("page") int page) {
        log.info("Start");
        return ApiResponse.<List<CustomerFranchiseResponse>>builder()
                .statusCode(200)
                .message("Get all customer")
                .data(customerService.getAll(page))
                .build();
    }

    @GetMapping("/admin/all")
    public ApiResponse<PageResponse<CustomerFranchiseResponse>> getAllCustomersForAdmin(
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<PageResponse<CustomerFranchiseResponse>>builder()
                .statusCode(200)
                .message("Get all customers successfully")
                .data(customerService.getCustomersForAdmin(status, pageable))
                .build();
    }

    @GetMapping("/franchise/all")
    public ApiResponse<PageResponse<CustomerFranchiseResponse>> getCustomersForManager(
            @RequestParam UUID franchiseId,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<PageResponse<CustomerFranchiseResponse>>builder()
                .statusCode(200)
                .message("Get franchise customers successfully")
                .data(customerService.getCustomersForManager(franchiseId, status, pageable))
                .build();
    }

    // ================== READ ==================

    // ================== SEARCH & FILTER ==================
    @GetMapping("/search")
    public ApiResponse<PageResponse<CustomerFranchiseResponse>> searchCustomers(
            @RequestParam(required = false) UUID franchiseId,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) List<UUID> userIds,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.<PageResponse<CustomerFranchiseResponse>>builder()
                .statusCode(200)
                .message("Search customers successfully")
                .data(customerService.searchCustomers(franchiseId, status, userIds, pageable))
                .build();
    }

    @PostMapping("/bulk")
    public ApiResponse<List<CustomerFranchiseResponse>> getCustomersByIds(@RequestBody List<UUID> ids) {
        return ApiResponse.<List<CustomerFranchiseResponse>>builder()
                .statusCode(200)
                .message("Get customers by ids successfully")
                .data(customerService.getCustomersByIds(ids))
                .build();
    }

//    @GetMapping("/{id}")
    @GetMapping("/customer-franchise")
    public ApiResponse<CustomerFranchiseResponse> getCustomerDetail(
            @PathParam("userId") UUID userId,
            @PathParam("franchiseId") UUID franchiseId
    ) {
        return ApiResponse.<CustomerFranchiseResponse>builder()
                .statusCode(200)
                .message("Get customer of franchise by customer id")
                .data(customerService.getCustomerOfFranchiseById(userId, franchiseId))
                .build();
    }

    @PostMapping("/save-customer-franchise")
    public ApiResponse<CustomerFranchise> saveCustomerFranchise(@RequestBody SaveCustomerRequest request ) {
        if (request.getFranchiseId() == null || request.getCustomerId() == null) {
            throw new AppException(ErrorCode.DATA_NULL);
        }

        return ApiResponse.<CustomerFranchise>builder()
                .statusCode(201)
                .message("Create customer of franchise")
                .data(customerService.saveCustomerFranchise(request))
                .build();
    }

    // API Nội bộ: Identity Service gọi sang khi user tự đăng ký App
    @PostMapping("/internal/sync")
    public ApiResponse<Void> syncCustomerFromIdentity(
            @RequestParam UUID userId,
            @RequestParam(required = false) CustomerType type) {
        customerService.syncCustomerFromIdentity(userId, type);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Sync customer successfully")
                .build();
    }

    // ================== UPDATE ==================
    @PutMapping("/{id}")
    public ApiResponse<CustomerFranchise> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ApiResponse.<CustomerFranchise>builder()
                .statusCode(200)
                .message("Update customer successfully")
                .data(customerService.updateCustomerFranchise(id, request))
                .build();
    }

    // UPDATE Status (Suspend / Active)
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

    // ================== SOFT DELETE ==================
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Delete customer successfully")
                .build();
    }

    @GetMapping("/searching")
    public ApiResponse<Page<CustomerSummaryResponse>> search(@ModelAttribute @Valid SearchRequest request) {
        return ApiResponse.<Page<CustomerSummaryResponse>>builder()
                .statusCode(200)
                .message("Search customer")
                .data(customerService.searchCustomers(request))
                .build();
    }


}