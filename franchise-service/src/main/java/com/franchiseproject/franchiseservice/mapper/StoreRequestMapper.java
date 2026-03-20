package com.franchiseproject.franchiseservice.mapper;

import com.franchiseproject.franchiseservice.dto.RequestItemDTO;
import com.franchiseproject.franchiseservice.dto.StoreRequestDTO;
import com.franchiseproject.franchiseservice.model.StoreRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class StoreRequestMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "franchiseId", source = "franchise.id")
    @Mapping(target = "franchiseName", source = "franchise.name")
    @Mapping(target = "requestData", source = "requestData", qualifiedByName = "mapToRequestData")
    @Mapping(target = "items", source = "requestData", qualifiedByName = "extractItems")
    @Mapping(target = "notes", source = "requestData", qualifiedByName = "extractNotes")
    @Mapping(target = "totalAmount", source = "requestData", qualifiedByName = "extractTotalAmount")
    @Mapping(target = "customerName", source = "requestData", qualifiedByName = "extractCustomerName")
    // Các field còn lại MapStruct sẽ tự động map (id, requestCode, status, adminNotes, reviewedBy, reviewedAt, createdAt, updatedAt)
    public abstract StoreRequestDTO toDTO(StoreRequest storeRequest);

    /**
     * Map JSON string sang Map<String, Object>
     */
    @Named("mapToRequestData")
    protected Map<String, Object> mapToRequestData(String requestData) {
        if (requestData == null || requestData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(requestData, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse requestData JSON: " + requestData, e);
        }
    }

    @Named("extractItems")
    protected List<RequestItemDTO> extractItems(String requestData) {
        if (requestData == null) return null;
        try {
            Map<String, Object> data = objectMapper.readValue(requestData, new TypeReference<Map<String, Object>>() {});
            Object itemsObj = data.get("items");
            if (itemsObj != null) {
                String itemsJson = objectMapper.writeValueAsString(itemsObj);
                return objectMapper.readValue(itemsJson, new TypeReference<List<RequestItemDTO>>() {});
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Named("extractNotes")
    protected String extractNotes(String requestData) {
        if (requestData == null) return null;
        try {
            Map<String, Object> data = objectMapper.readValue(requestData, new TypeReference<Map<String, Object>>() {});
            return (String) data.get("notes");
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("extractTotalAmount")
    protected BigDecimal extractTotalAmount(String requestData) {
        if (requestData == null) return null;
        try {
            Map<String, Object> data = objectMapper.readValue(requestData, new TypeReference<Map<String, Object>>() {});
            Object total = data.get("total_amount");
            if (total instanceof Integer) {
                return BigDecimal.valueOf((Integer) total);
            } else if (total instanceof Double) {
                return BigDecimal.valueOf((Double) total);
            } else if (total instanceof String) {
                return new BigDecimal((String) total);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Named("extractCustomerName")
    protected String extractCustomerName(String requestData) {
        if (requestData == null) return null;
        try {
            Map<String, Object> data = objectMapper.readValue(requestData, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> customerInfo = (Map<String, Object>) data.get("customer_info");
            if (customerInfo != null) {
                return (String) customerInfo.get("full_name");
            }
        } catch (JsonProcessingException e) {
            return null;
        }
        return null;
    }
}