package com.byteforge.setorabackend.business.mapper;

import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.Location;
import com.byteforge.setorabackend.business.dtos.BusinessLocationDto;
import com.byteforge.setorabackend.business.dtos.BusinessResponseDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class BusinessMapper {

    public BusinessResponseDto toResponseDto(Business business) {
        return new BusinessResponseDto(
                business.getId(),
                business.getOwner().getId(),
                business.getName(),
                business.getSlug(),
                business.getDescription(),
                toLocationDto(business.getLocation()),
                business.getPhone(),
                business.getEmail(),
                business.getTimezone().getId(),
                business.getCreatedAt(),
                toList(business.getPhotoUrls()),
                business.getThumbnailUrl(),
                toList(business.getTags())
        );
    }

    private BusinessLocationDto toLocationDto(Location location) {
        if (location == null) {
            return new BusinessLocationDto(null, null, null, null);
        }
        return new BusinessLocationDto(
                location.getLocationName(),
                location.getAddress(),
                location.getLatitude(),
                location.getLongitude()
        );
    }

    private List<String> toList(String[] array) {
        return array == null ? List.of() : Arrays.asList(array);
    }
}
