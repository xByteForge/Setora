package com.byteforge.setorabackend.business;

import com.byteforge.setorabackend.appointment.AppointmentRepository;
import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.business.dtos.AppointmentReferenceDataDto;
import com.byteforge.setorabackend.business.dtos.BusinessLocationDto;
import com.byteforge.setorabackend.business.dtos.ClientReferenceDto;
import com.byteforge.setorabackend.business.dtos.EmployeeReferenceDto;
import com.byteforge.setorabackend.business.dtos.ServiceReferenceDto;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.repository.EmployeeRepository;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessReferenceService {

    private final BusinessService businessService;
    private final EmployeeRepository employeeRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<EmployeeReferenceDto> getEmployees(UUID businessId) {
        businessService.findById(businessId);
        List<Employee> employees = employeeRepository.findByBusinessIdAndActiveTrue(businessId);
        return employees.stream()
                .map(EmployeeReferenceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BusinessLocationDto> getLocations(UUID businessId) {
        Business business = businessService.findById(businessId);
        return List.of(BusinessLocationDto.from(business));
    }

    @Transactional(readOnly = true)
    public BusinessLocationDto getLocation(UUID businessId) {
        Business business = businessService.findById(businessId);
        return BusinessLocationDto.from(business);
    }

    @Transactional(readOnly = true)
    public List<ServiceReferenceDto> getServices(UUID businessId) {
        businessService.findById(businessId);
        List<ServiceItem> services = serviceItemRepository.findByBusinessIdAndActiveTrue(businessId);
        return services.stream()
                .map(ServiceReferenceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceReferenceDto> searchServices(UUID businessId, String query) {
        businessService.findById(businessId);
        String nameFilter = (query != null && !query.isBlank()) ? query.trim() : null;
        List<ServiceItem> services = serviceItemRepository.searchServices(businessId, nameFilter);
        return services.stream()
                .map(ServiceReferenceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientReferenceDto> getClients(UUID businessId) {
        return searchClients(businessId, null);
    }

    @Transactional(readOnly = true)
    public List<ClientReferenceDto> searchClients(UUID businessId, String query) {
        businessService.findById(businessId);
        String nameFilter = (query != null && !query.isBlank()) ? query.trim() : null;
        List<User> clients = appointmentRepository.searchClientsByBusiness(businessId, nameFilter);
        return clients.stream()
                .map(ClientReferenceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentReferenceDataDto getAppointmentReferenceData(UUID businessId) {
        List<BusinessLocationDto> locations = getLocations(businessId);
        List<EmployeeReferenceDto> employees = getEmployees(businessId);
        List<ServiceReferenceDto> services = getServices(businessId);
        return new AppointmentReferenceDataDto(locations, employees, services);
    }
}

