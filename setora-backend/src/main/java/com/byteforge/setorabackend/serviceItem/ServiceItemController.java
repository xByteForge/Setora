package com.byteforge.setorabackend.serviceItem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ServiceItemController {
    final private ServiceItemService serviceItemService;

}
