package com.byteforge.setorabackend.business.service;

import com.byteforge.setorabackend.business.dtos.BusinessDetailedResponseDto;
import com.byteforge.setorabackend.business.dtos.BusinessFilterCriteria;
import com.byteforge.setorabackend.business.dtos.BusinessResponseDto;
import com.byteforge.setorabackend.business.dtos.BusinessSortBy;
import com.byteforge.setorabackend.business.mapper.BusinessMapper;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.repository.BusinessRepository;
import com.byteforge.setorabackend.business.repository.BusinessSpecification;
import com.byteforge.setorabackend.common.PaginationParams;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;

    private final BusinessMapper businessMapper;

    public Business findById(int businessId) {
        return businessRepository
                .findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));
    }

    @Transactional(readOnly = true)
    public BusinessDetailedResponseDto getBusinessById(int businessId) {
        return businessMapper.toDetailedResponseDto(findById(businessId));
    }

    @Transactional(readOnly = true)
    public Page<BusinessResponseDto> getBusinesses(
            BusinessFilterCriteria criteria, BusinessSortBy sortBy, PaginationParams pageParams) {
        BusinessSortBy effectiveSort = (sortBy != null) ? sortBy : BusinessSortBy.NEWEST;
        boolean isPriceSort = effectiveSort == BusinessSortBy.PRICE_ASC || effectiveSort == BusinessSortBy.PRICE_DESC;

        Pageable pageable = isPriceSort
                ? PageRequest.of(pageParams.page(), pageParams.size())
                : PageRequest.of(pageParams.page(), pageParams.size(), buildStandardSort(effectiveSort));

        Specification<Business> spec = BusinessSpecification.withCriteria(criteria)
                .and(BusinessSpecification.withPriceSort(criteria, effectiveSort));

        return businessRepository.findAll(spec, pageable).map(businessMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<BusinessResponseDto> getAllBusinesses(PaginationParams pageParams) {
        return getBusinesses(null, null, pageParams);
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableLocations() {
        return businessRepository.findDistinctLocations();
    }

    private Sort buildStandardSort(BusinessSortBy sortBy) {
        return switch (sortBy) {
            case NAME_ASC -> Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"));
            case NAME_DESC -> Sort.by(Sort.Direction.DESC, "name").and(Sort.by(Sort.Direction.DESC, "id"));
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
            case NEWEST, PRICE_ASC, PRICE_DESC ->
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }
}
