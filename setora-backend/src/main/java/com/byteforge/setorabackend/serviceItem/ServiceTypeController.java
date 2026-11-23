package com.byteforge.setorabackend.serviceItem;

import com.byteforge.setorabackend.serviceItem.dtos.ServiceTypeDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services/types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping
    public ResponseEntity<List<ServiceTypeDto>> getServiceTypes(@RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(serviceTypeService.getServiceTypes(categoryId));
    }
}
