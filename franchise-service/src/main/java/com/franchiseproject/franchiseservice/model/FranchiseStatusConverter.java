package com.franchiseproject.franchiseservice.model;

import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FranchiseStatusConverter implements AttributeConverter<FranchiseStatus, String> {

    @Override
    public String convertToDatabaseColumn(FranchiseStatus attribute) {
        if (attribute == null) {
            return null;
        }
        // Chuyển NEW -> new, ACTIVE -> active, INACTIVE -> inactive
        return attribute.name().toLowerCase();
    }

    @Override
    public FranchiseStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Chuyển new -> NEW, active -> ACTIVE, inactive -> INACTIVE
        return FranchiseStatus.valueOf(dbData.toUpperCase());
    }
}