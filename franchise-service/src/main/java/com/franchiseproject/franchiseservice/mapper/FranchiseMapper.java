package com.franchiseproject.franchiseservice.mapper;

import com.franchiseproject.franchiseservice.dto.FranchiseDTO;
import com.franchiseproject.franchiseservice.model.Franchise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FranchiseMapper {

    FranchiseDTO toDTO(Franchise franchise);

    Franchise toEntity(FranchiseDTO franchiseDTO);
}