package com.byteforge.setorabackend.business.dtos;

import java.util.List;

public record AppointmentReferenceDataDto(
        List<BusinessLocationDto> locations,
        List<EmployeeReferenceDto> employees,
        List<ServiceReferenceDto> services
) {}

