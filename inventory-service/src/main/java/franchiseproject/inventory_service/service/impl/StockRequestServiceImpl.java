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
import franchiseproject.inventory_service.service.StockRequestService;
import franchiseproject.inventory_service.client.ProductClient;
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
        if (response.getItems() != null) {
            response.getItems().forEach(item -> {
                try {
                    var apiRes = productClient.getProductVariant(item.getProductVariantId());
                    if (apiRes != null && apiRes.getData() != null) {
                        var detail = apiRes.getData();
                        item.setProductName(detail.getProductName());
                        item.setSize(detail.getSize() != null ? detail.getSize() : "N/A");
                        item.setColor(detail.getColor() != null ? detail.getColor() : "N/A");
                    }
                } catch (Exception e) {
                    System.err.println("Feign error StockRequestItem: " + e.getMessage());
                }
            });
        }
    }

    @Override
    public List<StockRequestResponse> getAllRequests() {
        List<StockRequestResponse> responses = stockRequestRepository.findAll().stream()
                .map(stockRequestMapper::toResponse)
                .collect(Collectors.toList());

        responses.forEach(this::enrichRequestItems);
        return responses;
    }

    @Override
    public StockRequestResponse getRequestById(UUID id) {
        StockRequest stockRequest = stockRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return stockRequestMapper.toResponse(stockRequest);
    }
}
