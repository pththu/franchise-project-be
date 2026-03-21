package com.franchiseproject.franchiseservice.service.impl;

import com.franchiseproject.franchiseservice.client.CustomerServiceClient;
import com.franchiseproject.franchiseservice.dto.CustomerInfoDTO;
import com.franchiseproject.franchiseservice.dto.StoreRequestDTO;
import com.franchiseproject.franchiseservice.enums.RequestStatus;
import com.franchiseproject.franchiseservice.exception.BadRequestException;
import com.franchiseproject.franchiseservice.exception.ResourceNotFoundException;
import com.franchiseproject.franchiseservice.mapper.StoreRequestMapper;
import com.franchiseproject.franchiseservice.model.Franchise;
import com.franchiseproject.franchiseservice.model.StoreRequest;
import com.franchiseproject.franchiseservice.repository.FranchiseRepository;
import com.franchiseproject.franchiseservice.repository.StoreRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchiseproject.franchiseservice.service.StoreRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreRequestServiceImpl implements StoreRequestService {

    private final StoreRequestRepository storeRequestRepository;
    private final FranchiseRepository franchiseRepository;
    private final StoreRequestMapper storeRequestMapper;
    private final CustomerServiceClient customerServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<StoreRequestDTO> getAllRequests() {
        return storeRequestRepository.findAll()
                .stream()
                .map(storeRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StoreRequestDTO getRequestById(Long id) {
        StoreRequest request = storeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        return storeRequestMapper.toDTO(request);
    }

    @Override
    public StoreRequestDTO getRequestByCode(String requestCode) {
        StoreRequest request = storeRequestRepository.findByRequestCode(requestCode)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with code: " + requestCode));
        return storeRequestMapper.toDTO(request);
    }

    @Override
    public List<StoreRequestDTO> getRequestsByFranchise(Long franchiseId) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + franchiseId));

        return storeRequestRepository.findByFranchise(franchise)
                .stream()
                .map(storeRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreRequestDTO> getRequestsByStatus(RequestStatus status) {
        return storeRequestRepository.findByStatus(status)
                .stream()
                .map(storeRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreRequestDTO reviewRequest(Long id, RequestStatus status, String adminNotes, Integer reviewedBy) {
        StoreRequest request = storeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        // Validate status transition
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be reviewed");
        }

        request.setStatus(status);
        request.setAdminNotes(adminNotes);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(LocalDateTime.now());

        StoreRequest updatedRequest = storeRequestRepository.save(request);
        log.info("Request {} reviewed by admin {} with status {}", request.getRequestCode(), reviewedBy, status);

        return storeRequestMapper.toDTO(updatedRequest);
    }

    @Override
    @Transactional
    public StoreRequestDTO createRequest(StoreRequestDTO requestDTO) {
        // Validate franchise
        Franchise franchise = franchiseRepository.findById(requestDTO.getFranchiseId())
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with id: " + requestDTO.getFranchiseId()));

        // Validate customer từ customer-service
        try {
            CustomerInfoDTO customerInfo = customerServiceClient.getCustomerById(requestDTO.getCustomerId());
            if (customerInfo == null) {
                throw new BadRequestException("Invalid customer id: " + requestDTO.getCustomerId());
            }

            // Tạo request code
            String requestCode = generateRequestCode();

            // Build request data JSON với customer info snapshot
            Map<String, Object> requestData = buildRequestData(requestDTO, customerInfo);

            StoreRequest storeRequest = new StoreRequest();
            storeRequest.setRequestCode(requestCode);
            storeRequest.setFranchise(franchise);
            storeRequest.setCustomerId(requestDTO.getCustomerId());
            storeRequest.setRequestDate(LocalDate.now());
            storeRequest.setRequestData(objectMapper.writeValueAsString(requestData));
            storeRequest.setStatus(RequestStatus.PENDING);

            StoreRequest savedRequest = storeRequestRepository.save(storeRequest);
            log.info("New request created: {} by customer {}", requestCode, requestDTO.getCustomerId());

            return storeRequestMapper.toDTO(savedRequest);

        } catch (JsonProcessingException e) {
            log.error("Error processing request data", e);
            throw new RuntimeException("Error processing request data", e);
        } catch (Exception e) {
            log.error("Error creating request", e);
            throw new BadRequestException("Error creating request: " + e.getMessage());
        }
    }

    @Override
    public List<StoreRequestDTO> getRequestsByCustomer(Integer customerId) {
        return storeRequestRepository.findByCustomerId(customerId)
                .stream()
                .map(storeRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreRequestDTO> getRequestsByCustomerAndStatus(Integer customerId, RequestStatus status) {
        return storeRequestRepository.findByCustomerIdAndStatus(customerId, status)
                .stream()
                .map(storeRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    private String generateRequestCode() {
        String code;
        do {
            code = "REQ" + LocalDate.now().toString().replace("-", "") + "-" +
                    UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (storeRequestRepository.existsByRequestCode(code));
        return code;
    }

    private Map<String, Object> buildRequestData(StoreRequestDTO dto, CustomerInfoDTO customerInfo) {
        Map<String, Object> data = new java.util.HashMap<>();

        // Customer info snapshot
        Map<String, Object> customerSnapshot = new java.util.HashMap<>();
        customerSnapshot.put("customer_id", customerInfo.getId());
        customerSnapshot.put("full_name", customerInfo.getFullName());
        customerSnapshot.put("email", customerInfo.getEmail());
        customerSnapshot.put("phone", customerInfo.getPhone());
        data.put("customer_info", customerSnapshot);

        // Items
        data.put("items", dto.getItems());

        // Notes
        data.put("notes", dto.getNotes());

        // Total amount
        data.put("total_amount", dto.getTotalAmount());

        return data;
    }
}