package com.franchiseproject.shiftservice.service.impl;


import com.franchiseproject.shiftservice.model.ShiftConfiguration;
import com.franchiseproject.shiftservice.repository.ShiftConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftConfigurationService {
    private final ShiftConfigurationRepository repository;

    public List<ShiftConfiguration> getAllShifts() {
        return repository.findAll();
    }
}
