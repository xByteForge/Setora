package com.byteforge.setorabackend.business;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class BusinessService {
    final private BusinessRepository businessRepository;

    public Business findById(int businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

    }


}