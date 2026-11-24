package com.byteforge.setorabackend.serviceItem;

import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @GetMapping
    public ResponseEntity<List<ServiceResponseDto>> getServicesByBusinessId(@RequestParam int businessId) {
        return ResponseEntity.ok(serviceItemService.getServicesByBusinessId(businessId));
    }
}
