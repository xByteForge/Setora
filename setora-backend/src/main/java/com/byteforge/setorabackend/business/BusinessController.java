package com.byteforge.setorabackend.business;

import com.byteforge.setorabackend.business.dtos.BusinessFilterCriteria;
import com.byteforge.setorabackend.business.dtos.BusinessResponseDto;
import com.byteforge.setorabackend.business.dtos.BusinessSortBy;
import com.byteforge.setorabackend.common.PaginationParams;
import com.byteforge.setorabackend.employees.model.EmployeeResponseDto;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;
    private final ServiceItemService serviceItemService;
    private final EmployeeService employeeService;

    @GetMapping("/{businessId}")
    public ResponseEntity<BusinessResponseDto> getBusinessById(@PathVariable int businessId) {
        return ResponseEntity.ok(businessService.getBusinessById(businessId));
    }

    @GetMapping("/{businessId}/services")
    public ResponseEntity<List<ServiceResponseDto>> getServicesByBusinessId(@PathVariable int businessId) {
        return ResponseEntity.ok(serviceItemService.getServicesByBusinessId(businessId));
    }

    @GetMapping("/{businessId}/employees")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByBusinessId(@PathVariable int businessId) {
        return ResponseEntity.ok(employeeService.getEmployeesByBusinessId(businessId));
    }

    @GetMapping
    public ResponseEntity<Page<BusinessResponseDto>> getBusinesses(
            @Valid @ModelAttribute BusinessFilterCriteria criteria,
            @RequestParam(required = false, defaultValue = "NEWEST") BusinessSortBy sortBy,
            @Valid @ModelAttribute PaginationParams pageParams) {
        return ResponseEntity.ok(businessService.getBusinesses(criteria, sortBy, pageParams));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<BusinessResponseDto>> getAllBusinesses(
            @Valid @ModelAttribute PaginationParams pageParams) {
        return ResponseEntity.ok(businessService.getAllBusinesses(pageParams));
    }
}

