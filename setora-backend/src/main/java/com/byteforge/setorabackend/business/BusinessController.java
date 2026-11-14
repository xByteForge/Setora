package com.byteforge.setorabackend.business;

import com.byteforge.setorabackend.business.dtos.AppointmentReferenceDataDto;
import com.byteforge.setorabackend.business.dtos.BusinessLocationDto;
import com.byteforge.setorabackend.business.dtos.ClientReferenceDto;
import com.byteforge.setorabackend.business.dtos.EmployeeReferenceDto;
import com.byteforge.setorabackend.business.dtos.ServiceReferenceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessReferenceService businessReferenceService;

    @GetMapping(value = {
            "/api/businesses/{businessId}/employees",
            "/api/employees"
    })
    public ResponseEntity<List<EmployeeReferenceDto>> getEmployees(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        return ResponseEntity.ok(businessReferenceService.getEmployees(businessId));
    }

    @GetMapping(value = {
            "/api/businesses/{businessId}/locations",
            "/api/locations"
    })
    public ResponseEntity<List<BusinessLocationDto>> getLocations(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        return ResponseEntity.ok(businessReferenceService.getLocations(businessId));
    }

    @GetMapping("/api/businesses/{businessId}/location")
    public ResponseEntity<BusinessLocationDto> getLocation(
            @PathVariable UUID businessId
    ) {
        return ResponseEntity.ok(businessReferenceService.getLocation(businessId));
    }

    @GetMapping(value = {
            "/api/businesses/{businessId}/services",
            "/api/services"
    })
    public ResponseEntity<List<ServiceReferenceDto>> getServices(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "query", required = false) String query
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        String filter = resolveFilter(name, query);
        if (filter != null) {
            return ResponseEntity.ok(businessReferenceService.searchServices(businessId, filter));
        }
        return ResponseEntity.ok(businessReferenceService.getServices(businessId));
    }

    @GetMapping(value = {
            "/api/businesses/{businessId}/services/search",
            "/api/services/search"
    })
    public ResponseEntity<List<ServiceReferenceDto>> searchServices(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "query", required = false) String query
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        String filter = resolveFilter(name, query);
        return ResponseEntity.ok(businessReferenceService.searchServices(businessId, filter));
    }

    @GetMapping(value = {
            "/api/businesses/{businessId}/clients",
            "/api/clients"
    })
    public ResponseEntity<List<ClientReferenceDto>> getClients(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "query", required = false) String query
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        String filter = resolveFilter(name, query);
        return ResponseEntity.ok(businessReferenceService.searchClients(businessId, filter));
    }

    @GetMapping(value = {
            "/api/businesses/{businessId}/clients/search",
            "/api/clients/search"
    })
    public ResponseEntity<List<ClientReferenceDto>> searchClients(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "query", required = false) String query
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        String filter = resolveFilter(name, query);
        return ResponseEntity.ok(businessReferenceService.searchClients(businessId, filter));
    }

    @GetMapping(value = {
            "/api/businesses/{businessId}/reference-data",
            "/api/appointments/reference-data"
    })
    public ResponseEntity<AppointmentReferenceDataDto> getAppointmentReferenceData(
            @PathVariable(name = "businessId", required = false) UUID pathBusinessId,
            @RequestParam(name = "businessId", required = false) UUID paramBusinessId
    ) {
        UUID businessId = resolveBusinessId(pathBusinessId, paramBusinessId);
        return ResponseEntity.ok(businessReferenceService.getAppointmentReferenceData(businessId));
    }

    private UUID resolveBusinessId(UUID pathId, UUID paramId) {
        UUID id = pathId != null ? pathId : paramId;
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "businessId is required");
        }
        return id;
    }

    private String resolveFilter(String name, String query) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        if (query != null && !query.isBlank()) {
            return query.trim();
        }
        return null;
    }
}

