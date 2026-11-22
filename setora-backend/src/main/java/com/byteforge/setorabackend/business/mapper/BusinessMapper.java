package com.byteforge.setorabackend.business.mapper;

import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.model.Category;
import com.byteforge.setorabackend.business.model.Location;
import com.byteforge.setorabackend.business.dtos.BusinessLocationDto;
import com.byteforge.setorabackend.business.dtos.BusinessResponseDto;
import java.util.Arrays;
import java.util.List;

import com.byteforge.setorabackend.business.dtos.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public BusinessResponseDto toResponseDto(Business business) {
        return new BusinessResponseDto(
                business.getId(),
                business.getOwner().getId(),
                toCategoryDto(business.getCategory()),
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
                toList(business.getTags()));
    }

    private BusinessLocationDto toLocationDto(Location location) {
        if (location == null) {
            return new BusinessLocationDto(null, null, null, null);
        }
        return new BusinessLocationDto(
                location.getLocationName(), location.getAddress(), location.getLatitude(), location.getLongitude());
    }

    private CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(category.getId(), category.getName());
    }

    private List<String> toList(String[] array) {
        return array == null ? List.of() : Arrays.asList(array);
    }
}
