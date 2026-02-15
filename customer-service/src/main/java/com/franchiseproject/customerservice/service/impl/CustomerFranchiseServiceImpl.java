package com.franchiseproject.customerservice.service.impl;

import com.franchiseproject.customerservice.model.CustomerFranchise;
import com.franchiseproject.customerservice.repository.CustomerFranchiseRepository;
import com.franchiseproject.customerservice.service.CustomerFranchiseService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CustomerFranchiseServiceImpl implements CustomerFranchiseService {
    CustomerFranchiseRepository customerFranchiseRepository;

    @Override
    public List<CustomerFranchise> getAll() {
        return customerFranchiseRepository.findAll();
    }
}
