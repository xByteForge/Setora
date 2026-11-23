package com.byteforge.setorabackend.serviceItem;

import com.byteforge.setorabackend.business.service.CategoryService;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceTypeDto;
import com.byteforge.setorabackend.serviceItem.mapper.ServiceItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    private final CategoryService categoryService;

    private final ServiceItemMapper serviceItemMapper;

    @Transactional(readOnly = true)
    public List<ServiceTypeDto> getServiceTypes(Integer categoryId) {
        if (categoryId != null) {
            categoryService.existsById(categoryId);
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<ServiceType> types = (categoryId != null)
                ? serviceTypeRepository.findByCategoryId(categoryId, sort)
                : serviceTypeRepository.findAll(sort);

        return types.stream()
                .map(serviceItemMapper::toServiceTypeDto)
                .toList();
    }
}