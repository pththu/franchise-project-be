package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.dto.request.CreateStockRequest;
import franchiseproject.inventory_service.dto.response.NotificationDTO;
import franchiseproject.inventory_service.dto.response.StockRequestResponse;
import franchiseproject.inventory_service.entity.StockRequest;
import franchiseproject.inventory_service.entity.StockRequestItem;
import franchiseproject.inventory_service.enums.StockRequestStatus;
import franchiseproject.inventory_service.exception.AppException;
import franchiseproject.inventory_service.exception.ErrorCode;
import franchiseproject.inventory_service.mapper.StockRequestMapper;
import franchiseproject.inventory_service.repository.StockRequestRepository;
import franchiseproject.inventory_service.repository.StockTransferRepository;
import franchiseproject.inventory_service.repository.ProductStockRepository;
import franchiseproject.inventory_service.service.StockRequestService;
import franchiseproject.inventory_service.client.ProductClient;
import franchiseproject.inventory_service.service.StockTransferService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockRequestServiceImpl implements StockRequestService {

    StockRequestRepository stockRequestRepository;
    StockRequestMapper stockRequestMapper;
    SimpMessagingTemplate messagingTemplate;
    ProductClient productClient;
    StockTransferService stockTransferService;
    StockTransferRepository stockTransferRepository;
    ProductStockRepository productStockRepository;

    @Override
    @Transactional
    public StockRequestResponse createRequest(CreateStockRequest request) {
        StockRequest stockRequest = stockRequestMapper.toEntity(request);
        
        // Generate requestCode: REQ-yyyyMMdd-XXXX
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        stockRequest.setRequestCode("REQ-" + datePart + "-" + randomPart);
        stockRequest.setStatus(StockRequestStatus.PENDING);

        List<StockRequestItem> items = request.getItems().stream()
                .map(itemReq -> {
                    StockRequestItem item = stockRequestMapper.toItemEntity(itemReq);
                    item.setStockRequest(stockRequest);
                    return item;
                })
                .collect(Collectors.toList());
        
        stockRequest.setItems(items);
        
        StockRequest savedRequest = stockRequestRepository.save(stockRequest);
        StockRequestResponse response = stockRequestMapper.toResponse(savedRequest);

        // Enrich items details
        enrichRequestItems(response);

        // Send real-time notification via WebSocket
        NotificationDTO<StockRequestResponse> notification = NotificationDTO.<StockRequestResponse>builder()
                .type("NEW_STOCK_REQUEST")
                .message("Có yêu cầu nhập hàng mới từ chi nhánh: " + request.getFranchiseId())
                .payload(response)
                .build();
        
        messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
        
        return response;
    }

    private void enrichRequestItems(StockRequestResponse response) {
        enrichRequests(java.util.Collections.singletonList(response));
    }

    private void enrichRequests(List<StockRequestResponse> responses) {
        if (responses == null || responses.isEmpty()) return;

        // Collect all variant IDs
        List<UUID> variantIds = responses.stream()
                .flatMap(r -> r.getItems() != null ? r.getItems().stream() : java.util.stream.Stream.empty())
                .map(franchiseproject.inventory_service.dto.response.StockRequestItemResponse::getProductVariantId)
                .distinct()
                .collect(Collectors.toList());

        if (variantIds.isEmpty()) return;

        // Bulk fetch product details
        java.util.Map<UUID, franchiseproject.inventory_service.dto.response.ProductVariantDetailResponse> productMap = new java.util.HashMap<>();
        try {
            var apiRes = productClient.getProductVariantsBulk(variantIds);
            if (apiRes != null && apiRes.getData() != null) {
                productMap = apiRes.getData().stream()
                        .collect(Collectors.toMap(franchiseproject.inventory_service.dto.response.ProductVariantDetailResponse::getId, d -> d));
            }
        } catch (Exception e) {
            System.err.println("Feign bulk error StockRequest enrich: " + e.getMessage());
        }

        final java.util.Map<UUID, franchiseproject.inventory_service.dto.response.ProductVariantDetailResponse> finalProductMap = productMap;

        // Process each response
        responses.forEach(response -> {
            UUID sourceId = null;
            var transfers = stockTransferRepository.findByReferenceRequestId(response.getId());
            var activeTransfer = transfers.stream()
                    .filter(t -> t.getStatus() != franchiseproject.inventory_service.enums.TransferStatus.CANCELLED)
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .findFirst();
            
            if (activeTransfer.isPresent()) {
                sourceId = activeTransfer.get().getFromLocationId();
                response.setSourceLocationId(sourceId);
            }
            final UUID finalSourceId = sourceId;

            if (response.getItems() != null) {
                response.getItems().forEach(item -> {
                    var detail = finalProductMap.get(item.getProductVariantId());
                    if (detail != null) {
                        item.setProductName(detail.getProductName());
                        item.setSize(detail.getSize() != null ? detail.getSize() : "N/A");
                        item.setColor(detail.getColor() != null ? detail.getColor() : "N/A");
                    }
                    if (finalSourceId != null) {
                        var stockOpt = productStockRepository.findByProductVariantIdAndLocationId(item.getProductVariantId(), finalSourceId);
                        item.setCurrentQuantity(stockOpt.map(f -> f.getQuantity()).orElse(0));
                    }
                });
            }
        });
    }

    @Override
    public List<StockRequestResponse> getAllRequests() {
        List<StockRequestResponse> responses = stockRequestRepository.findAll().stream()
                .map(stockRequestMapper::toResponse)
                .collect(Collectors.toList());

        enrichRequests(responses);
        return responses;
    }

    @Override
    public StockRequestResponse getRequestById(UUID id) {
        StockRequest stockRequest = stockRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        StockRequestResponse response = stockRequestMapper.toResponse(stockRequest);
        enrichRequestItems(response);
        return response;
    }

    @Override
    @Transactional
    public StockRequestResponse approveRequest(UUID id, UUID sourceLocationId, UUID approvedBy) {
        StockRequest req = stockRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (req.getStatus() != StockRequestStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        UUID nullUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        var transferReq = franchiseproject.inventory_service.dto.request.CreateStockTransferRequest.builder()
                .fromLocationId(sourceLocationId)
                .toLocationId(req.getFranchiseId())
                .type(sourceLocationId.equals(nullUuid) ? "WAREHOUSE_TO_FRANCHISE" : "FRANCHISE_TO_FRANCHISE")
                .referenceRequestId(req.getId())
                .notes("Duyệt tự động từ " + req.getRequestCode())
                .createdBy(req.getCreatedBy())
                .items(req.getItems().stream().map(i -> franchiseproject.inventory_service.dto.request.StockTransferItemRequest.builder()
                        .productVariantId(i.getProductVariantId())
                        .quantity(i.getQuantity())
                        .build()).collect(java.util.stream.Collectors.toList()))
                .build();

        stockTransferService.createTransfer(transferReq);

        req.setStatus(StockRequestStatus.APPROVED);
        req.setApprovedBy(approvedBy);
        StockRequest saved = stockRequestRepository.save(req);
        StockRequestResponse response = stockRequestMapper.toResponse(saved);
        enrichRequestItems(response);
        
        messagingTemplate.convertAndSend("/topic/admin/notifications", NotificationDTO.<StockRequestResponse>builder()
                .type("STOCK_REQUEST_APPROVED")
                .message("Yêu cầu duyệt " + req.getRequestCode())
                .payload(response)
                .build());

        return response;
    }

    @Override
    @Transactional
    public StockRequestResponse shipRequest(UUID id) {
        StockRequest req = stockRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (req.getStatus() != StockRequestStatus.APPROVED) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        var transfers = stockTransferRepository.findByReferenceRequestId(req.getId());
        franchiseproject.inventory_service.entity.StockTransfer transfer = transfers.stream()
                .filter(t -> t.getStatus() != franchiseproject.inventory_service.enums.TransferStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        stockTransferService.shipTransfer(transfer.getId());

        req.setStatus(StockRequestStatus.SHIPPED);
        StockRequest saved = stockRequestRepository.save(req);
        StockRequestResponse response = stockRequestMapper.toResponse(saved);
        enrichRequestItems(response);

        messagingTemplate.convertAndSend("/topic/admin/notifications", NotificationDTO.<StockRequestResponse>builder()
                .type("STOCK_REQUEST_SHIPPED")
                .message("Yêu cầu nhập hàng " + req.getRequestCode() + " đã xuất hàng")
                .payload(response)
                .build());

        return response;
    }

    @Override
    @Transactional
    public StockRequestResponse receiveRequest(UUID id) {
        StockRequest req = stockRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (req.getStatus() != StockRequestStatus.SHIPPED) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        var transfers = stockTransferRepository.findByReferenceRequestId(req.getId());
        franchiseproject.inventory_service.entity.StockTransfer transfer = transfers.stream()
                .filter(t -> t.getStatus() != franchiseproject.inventory_service.enums.TransferStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        stockTransferService.receiveTransfer(transfer.getId());

        req.setStatus(StockRequestStatus.RECEIVED);
        StockRequest saved = stockRequestRepository.save(req);
        StockRequestResponse response = stockRequestMapper.toResponse(saved);
        enrichRequestItems(response);

        messagingTemplate.convertAndSend("/topic/admin/notifications", NotificationDTO.<StockRequestResponse>builder()
                .type("STOCK_REQUEST_RECEIVED")
                .message("Yêu cầu nhập hàng " + req.getRequestCode() + " đã nhận được")
                .payload(response)
                .build());

        return response;
    }

    @Override
    @Transactional
    public StockRequestResponse rejectRequest(UUID id, String reason) {
        StockRequest req = stockRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (req.getStatus() != StockRequestStatus.PENDING && req.getStatus() != StockRequestStatus.APPROVED) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (req.getStatus() == StockRequestStatus.APPROVED) {
            var transfers = stockTransferRepository.findByReferenceRequestId(req.getId());
            var activeTransfer = transfers.stream()
                    .filter(t -> t.getStatus() != franchiseproject.inventory_service.enums.TransferStatus.CANCELLED)
                    .findFirst();
            if (activeTransfer.isPresent()) {
                var transfer = activeTransfer.get();
                transfer.setStatus(franchiseproject.inventory_service.enums.TransferStatus.CANCELLED);
                stockTransferRepository.save(transfer);
            }
        }

        req.setStatus(StockRequestStatus.REJECTED);
        if (reason != null && !reason.isBlank()) {
            req.setNotes(req.getNotes() != null ? req.getNotes() + " | Lý do từ chối: " + reason : "Lý do từ chối: " + reason);
        }
        StockRequest saved = stockRequestRepository.save(req);
        StockRequestResponse response = stockRequestMapper.toResponse(saved);
        enrichRequestItems(response);
        
        messagingTemplate.convertAndSend("/topic/admin/notifications", NotificationDTO.<StockRequestResponse>builder()
                .type("STOCK_REQUEST_REJECTED")
                .message("Yêu cầu từ chối " + req.getRequestCode())
                .payload(response)
                .build());

        return response;
    }

    @Override
    public List<StockRequestResponse> getRequestsByFranchiseId(UUID franchiseId) {
        List<StockRequestResponse> responses = stockRequestRepository.findByFranchiseId(franchiseId).stream()
                .map(stockRequestMapper::toResponse)
                .collect(Collectors.toList());

        enrichRequests(responses);
        return responses;
    }
}
